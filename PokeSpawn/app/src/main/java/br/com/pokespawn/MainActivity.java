package br.com.pokespawn;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final int INK = 0xff244c40, GREEN = 0xff4e8866, CREAM = 0xffd9e9c6;
    private static final int PAPER = 0xfffff9e9, MUTED = 0xff6e8173, RED = 0xffc55042, YELLOW = 0xffefc766;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService authWorker = Executors.newSingleThreadExecutor();
    private final Random random = new Random();
    private Encounter encounter = new Encounter();
    private PokemonApi api;
    private SpriteLoader sprites;
    private AccountStore accounts;
    private GameStore store;
    private SharedPreferences session;
    private Pokemon current;
    private String[] catalog = new String[Regions.TOTAL + 1];
    private Typeface pixel;
    private LinearLayout root;
    private TextView timer, captureStatus;
    private ProgressBar countdown;
    private Button captureButton;
    private DexAdapter dexAdapter;
    private boolean started, loading, destroyed, onlyCaught;
    private int screen, dexRegion, requestToken;
    private String spawnError, search = "";

    private final Runnable ticker = new Runnable() {
        @Override public void run() {
            if (!started || store == null) return;
            if (!loading && current != null && encounter.expired(SystemClock.elapsedRealtime())) loadEncounter();
            updateCountdown();
            main.postDelayed(this, 250);
        }
    };

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        try { pixel = Typeface.createFromAsset(getAssets(), "fonts/PixelifySans.ttf"); }
        catch (Exception error) { pixel = Typeface.MONOSPACE; }
        api = createPokemonApi(); sprites = new SpriteLoader(this); accounts = new AccountStore(this);
        session = getSharedPreferences("session", MODE_PRIVATE);
        String id = session.getString("id", null);
        if (id != null) store = new GameStore(this, id, session.getString("name", "Treinador"));
        if (saved != null && store != null && store.accountId.equals(saved.getString("account"))) {
            screen = saved.getInt("screen"); dexRegion = saved.getInt("dexRegion");
            search = saved.getString("search", ""); onlyCaught = saved.getBoolean("onlyCaught");
            try {
                String raw = saved.getString("pokemon");
                if (raw != null) {
                    current = new Pokemon(raw);
                    encounter.restore(current.id, saved.getLong("deadline"), saved.getBoolean("captured"));
                }
            } catch (Exception ignored) { current = null; }
        }
        render();
        api.catalog((value, error) -> {
            if (destroyed) return;
            if (value != null) { catalog = value; if (dexAdapter != null) dexAdapter.filter(); }
        });
    }
    protected PokemonApi createPokemonApi() { return new PokemonApi(this); }
    @Override protected void onStart() {
        super.onStart(); started = true;
        if (store != null) {
            if (current == null && !loading && spawnError == null) loadEncounter();
            main.removeCallbacks(ticker); main.post(ticker);
        }
    }
    @Override protected void onStop() { started = false; main.removeCallbacks(ticker); super.onStop(); }
    @Override protected void onDestroy() {
        destroyed = true; requestToken++; main.removeCallbacksAndMessages(null);
        api.close(); sprites.close(); authWorker.shutdownNow(); super.onDestroy();
    }
    @Override protected void onSaveInstanceState(Bundle saved) {
        super.onSaveInstanceState(saved);
        if (store == null) return;
        saved.putString("account", store.accountId); saved.putInt("screen", screen);
        saved.putInt("dexRegion", dexRegion); saved.putString("search", search); saved.putBoolean("onlyCaught", onlyCaught);
        if (current != null) {
            saved.putString("pokemon", current.json); saved.putLong("deadline", encounter.deadline());
            saved.putBoolean("captured", encounter.captured());
        }
    }

    private void render() {
        timer = null; countdown = null; captureButton = null; captureStatus = null; dexAdapter = null;
        root = column(); root.setBackgroundColor(CREAM);
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets.consumeSystemWindowInsets();
        });
        setContentView(root); root.requestApplyInsets();
        if (store == null) { login(); return; }
        LinearLayout header = row(); header.setPadding(dp(18), dp(14), dp(18), dp(14)); header.setBackgroundColor(INK);
        TextView brand = label("POKÉSPAWN", 13, PAPER); brand.setTypeface(pixel);
        header.addView(brand, new LinearLayout.LayoutParams(0, -2, 1));
        TextView badge = label(String.format(Locale.ROOT, "%03d DEX", store.unique(0)), 12, YELLOW);
        header.addView(badge); root.addView(header);
        if (screen == 1) pokedex();
        else if (screen == 2) profile();
        else capture();
        LinearLayout nav = row(); nav.setPadding(dp(8), dp(8), dp(8), dp(8)); nav.setBackgroundColor(INK);
        String[] labels = {"CAPTURA", "POKÉDEX", "PERFIL"};
        for (int i = 0; i < labels.length; i++) {
            final int target = i;
            Button button = button(labels[i], screen == i ? YELLOW : GREEN, screen == i ? INK : PAPER);
            button.setTextSize(13); button.setOnClickListener(v -> { screen = target; render(); });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(52), 1); lp.setMargins(dp(3), 0, dp(3), 0);
            nav.addView(button, lp);
        }
        root.addView(nav);
    }

    private void login() {
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true);
        LinearLayout page = column(); scroll.addView(page); root.addView(scroll, new LinearLayout.LayoutParams(-1, -1));
        page.addView(new LoginBanner(this), new LinearLayout.LayoutParams(-1, dp(220)));
        LinearLayout content = column(); content.setPadding(dp(26), dp(12), dp(26), dp(28)); page.addView(content);
        TextView start = label("PRESS START", 15, INK); start.setGravity(Gravity.CENTER); start.setTypeface(pixel, Typeface.BOLD); content.addView(start);
        space(content, 12);
        TextView logo = label("POKÉSPAWN", 33, INK); logo.setTypeface(pixel, Typeface.BOLD); logo.setGravity(Gravity.CENTER);
        content.addView(logo);
        TextView subtitle = label("Sua jornada começa aqui.", 14, MUTED); subtitle.setGravity(Gravity.CENTER); content.addView(subtitle);
        space(content, 38);
        LinearLayout form = card(); form.setPadding(dp(20), dp(20), dp(20), dp(20));
        form.addView(label("TREINADOR / E-MAIL", 13, INK)); space(form, 5);
        EditText name = input("Seu nome ou e-mail", false); name.setId(R.id.trainer_name);
        name.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(80)}); form.addView(name);
        space(form, 14); form.addView(label("SENHA", 13, INK)); space(form, 5);
        EditText password = input("Pelo menos 6 caracteres", true); password.setId(R.id.trainer_password); form.addView(password);
        TextView errorLabel = label("", 13, RED); errorLabel.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE); form.addView(errorLabel);
        Button enter = button("ENTRAR  →", GREEN, PAPER); enter.setId(R.id.login); form.addView(enter, fullButton()); content.addView(form);
        space(content, 20);
        Button register = button("NOVO TREINADOR? CRIAR CONTA", CREAM, INK); register.setTextSize(13); register.setId(R.id.register); content.addView(register, fullButton());
        View.OnClickListener submit = view -> {
            String username = name.getText().toString().trim();
            char[] secret = password.getText().toString().toCharArray();
            enter.setEnabled(false); register.setEnabled(false); errorLabel.setText("Conferindo treinador...");
            boolean create = view == register;
            authWorker.execute(() -> {
                String account = null; String error = null;
                try { account = accounts.authenticate(username, secret, create); }
                catch (Exception e) { error = e.getMessage(); }
                finally { Arrays.fill(secret, '\0'); }
                String accountId = account, message = error;
                main.post(() -> {
                    if (destroyed) return;
                    enter.setEnabled(true); register.setEnabled(true);
                    if (accountId == null) { errorLabel.setText(message); return; }
                    password.setText("");
                    ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(password.getWindowToken(), 0);
                    session.edit().putString("id", accountId).putString("name", username).apply();
                    store = new GameStore(this, accountId, username); screen = 0;
                    render(); loadEncounter(); main.removeCallbacks(ticker); main.post(ticker);
                });
            });
        };
        enter.setOnClickListener(submit); register.setOnClickListener(submit);
        space(content, 16);
        content.addView(label("Conta local • Progresso salvo neste aparelho\nInternet necessária para novos Pokémon.", 12, MUTED));
    }

    private void capture() {
        LinearLayout content = scrollContent();
        heading(content, "CAPTURE", "Um encontro. Uma nova descoberta.");
        content.addView(label("EXPLORAR REGIÃO", 11, MUTED));
        Spinner selector = regionSpinner(store.region());
        content.addView(selector, new LinearLayout.LayoutParams(-1, dp(48)));
        selector.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (store != null && position != store.region()) { store.region(position); loadEncounter(); }
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        space(content, 14);
        timer = label("NOVO ENCONTRO", 12, GREEN); timer.setTypeface(Typeface.MONOSPACE, Typeface.BOLD); content.addView(timer);
        countdown = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal); countdown.setMax(15_000);
        countdown.setProgressTintList(android.content.res.ColorStateList.valueOf(GREEN));
        content.addView(countdown, new LinearLayout.LayoutParams(-1, dp(10))); space(content, 12);
        LinearLayout card = card();
        TextView name = label(current == null ? (loading ? "Procurando..." : "Vamos explorar?") : current.title(), 23, INK);
        name.setTypeface(Typeface.MONOSPACE, Typeface.BOLD); card.addView(name);
        card.addView(label(current == null ? "UM POKÉMON SELVAGEM ESTÁ POR AQUI" : current.number() + "  •  " + current.types, 11, MUTED));
        space(card, 12);
        PixelScene scene = new PixelScene(this);
        ImageView sprite = new ImageView(this); sprite.setScaleType(ImageView.ScaleType.FIT_CENTER);
        sprite.setContentDescription(current == null ? "Aguardando Pokémon" : current.title());
        FrameLayout.LayoutParams imageLp = new FrameLayout.LayoutParams(dp(192), dp(192), Gravity.CENTER);
        imageLp.topMargin = dp(6); scene.addView(sprite, imageLp);
        if (current != null) sprites.show(sprite, current.sprite, true);
        else { sprite.setImageResource(R.drawable.ic_pokeball); sprite.setAlpha(.35f); }
        card.addView(scene, new LinearLayout.LayoutParams(-1, dp(240)));
        space(card, 14);
        captureStatus = label("", 13, MUTED); captureStatus.setMinHeight(dp(45));
        captureStatus.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE); card.addView(captureStatus);
        captureButton = button("CAPTURAR", GREEN, PAPER); captureButton.setId(R.id.capture);
        captureButton.setOnClickListener(v -> {
            if (current == null) { if (!loading) loadEncounter(); return; }
            long now = SystemClock.elapsedRealtime();
            if (!encounter.canCapture(now)) { if (encounter.expired(now)) loadEncounter(); return; }
            boolean newSpecies = !store.has(current.id);
            if (!store.add(current, System.currentTimeMillis())) {
                toast("Não foi possível salvar a captura. Tente novamente."); return;
            }
            encounter.capture(now);
            toast(newSpecies ? current.title() + " entrou na Pokédex!" : "Mais um " + current.title() + " capturado!");
            render();
        });
        card.addView(captureButton, fullButton()); content.addView(card);
        space(content, 16);
        content.addView(label("O Pokémon muda a cada 15 segundos.\nCapture o que está na tela para registrá-lo na Pokédex.", 12, MUTED));
        updateCountdown();
    }

    private void loadEncounter() {
        if (store == null || destroyed) return;
        final int token = ++requestToken;
        int id = Encounter.nextId(random, store.region(), encounter.id());
        loading = true; spawnError = null; current = null;
        if (started && screen == 0) render();
        api.pokemon(id, (pokemon, error) -> {
            if (destroyed || token != requestToken || store == null) return;
            loading = false;
            if (error != null) spawnError = "Sem conexão com a PokéAPI. Confira sua internet e tente novamente.";
            else { current = pokemon; encounter.show(pokemon.id, SystemClock.elapsedRealtime()); }
            if (started && screen == 0) render();
        });
    }

    private void updateCountdown() {
        if (timer == null || screen != 0 || store == null) return;
        long now = SystemClock.elapsedRealtime();
        if (loading || current == null) {
            timer.setText(loading ? "PROCURANDO UM POKÉMON..." : "ENCONTRO PAUSADO"); countdown.setProgress(0);
            captureStatus.setText(spawnError != null ? spawnError : "Buscando um encontro na região escolhida.");
            captureButton.setText(spawnError != null ? "TENTAR NOVAMENTE" : "CARREGANDO..."); captureButton.setEnabled(!loading);
        } else {
            timer.setText("PRÓXIMO POKÉMON EM " + encounter.seconds(now) + "s");
            countdown.setProgress((int) Math.max(0, encounter.deadline() - now));
            boolean caught = encounter.captured();
            captureButton.setEnabled(encounter.canCapture(now)); captureButton.setText(caught ? "CAPTURADO!" : "CAPTURAR");
            captureStatus.setText(caught ? "Salvo na sua Pokédex. Aguarde o próximo encontro!" : "Um " + current.title() + " selvagem apareceu!");
        }
    }

    private void pokedex() {
        LinearLayout content = column(); content.setPadding(dp(16), dp(18), dp(16), 0);
        root.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));
        heading(content, "POKÉDEX", store.unique(0) + " / " + Regions.TOTAL + " espécies descobertas");
        Spinner region = regionSpinner(dexRegion); content.addView(region, new LinearLayout.LayoutParams(-1, dp(48)));
        EditText query = input("Buscar capturado ou nº da Pokédex", false); query.setInputType(InputType.TYPE_CLASS_TEXT);
        query.setText(search); content.addView(query);
        Switch caughtOnly = new Switch(this); caughtOnly.setText("Somente capturados"); caughtOnly.setTextColor(INK);
        caughtOnly.setChecked(onlyCaught); caughtOnly.setMinHeight(dp(48)); content.addView(caughtOnly);
        content.addView(label("Colorido = capturado   •   Cinza = desconhecido", 10, MUTED)); space(content, 8);
        GridView grid = new GridView(this);
        int widthDp = (int) (getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().density);
        grid.setNumColumns(widthDp >= 600 ? 5 : 3);
        grid.setHorizontalSpacing(dp(7)); grid.setVerticalSpacing(dp(7)); grid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        TextView empty = label("Nenhum Pokémon encontrado.\nCapture um Pokémon ou ajuste os filtros.", 14, MUTED);
        FrameLayout area = new FrameLayout(this); area.addView(grid, new FrameLayout.LayoutParams(-1, -1));
        FrameLayout.LayoutParams emptyLp = new FrameLayout.LayoutParams(-1, -2, Gravity.CENTER);
        area.addView(empty, emptyLp); empty.setGravity(Gravity.CENTER); grid.setEmptyView(empty);
        dexAdapter = new DexAdapter(); grid.setAdapter(dexAdapter);
        content.addView(area, new LinearLayout.LayoutParams(-1, 0, 1));
        grid.setOnItemClickListener((parent, view, position, id) -> details((int) id));
        region.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                dexRegion = position; if (dexAdapter != null) dexAdapter.filter();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        query.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                search = s.toString(); if (dexAdapter != null) dexAdapter.filter();
            }
            @Override public void afterTextChanged(Editable editable) {}
        });
        caughtOnly.setOnCheckedChangeListener((button, checked) -> { onlyCaught = checked; dexAdapter.filter(); });
    }

    private class DexAdapter extends BaseAdapter {
        final List<Integer> ids = new ArrayList<>();
        DexAdapter() { filter(); }
        void filter() {
            ids.clear(); String query = search.trim().toLowerCase(Locale.ROOT).replace("#", "");
            for (int id = Regions.first(dexRegion); id <= Regions.last(dexRegion); id++) {
                boolean owned = store.has(id); if (onlyCaught && !owned) continue;
                String name = owned ? catalog[id] : "";
                if (owned && name == null) { Pokemon p = store.pokemon(id); name = p == null ? "" : p.name; }
                boolean matches = query.isEmpty() || Integer.toString(id).equals(query)
                    || String.format(Locale.ROOT, "%04d", id).contains(query)
                    || (name != null && name.toLowerCase(Locale.ROOT).contains(query));
                if (matches) ids.add(id);
            }
            notifyDataSetChanged();
        }
        @Override public int getCount() { return ids.size(); }
        @Override public Object getItem(int position) { return ids.get(position); }
        @Override public long getItemId(int position) { return ids.get(position); }
        @Override public boolean hasStableIds() { return true; }
        @Override public View getView(int position, View recycled, ViewGroup parent) {
            LinearLayout cell;
            if (recycled instanceof LinearLayout) cell = (LinearLayout) recycled;
            else {
                cell = column(); cell.setGravity(Gravity.CENTER); cell.setPadding(dp(5), dp(8), dp(5), dp(8));
                TextView number = label("", 10, MUTED); cell.addView(number);
                ImageView image = new ImageView(MainActivity.this); image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                cell.addView(image, new LinearLayout.LayoutParams(dp(78), dp(78)));
                TextView name = label("", 11, INK); name.setSingleLine(true); name.setEllipsize(android.text.TextUtils.TruncateAt.END); cell.addView(name);
            }
            int id = ids.get(position); boolean owned = store.has(id);
            Pokemon pokemon = owned ? store.pokemon(id) : null;
            cell.setBackground(border(owned ? PAPER : 0xffe5e7db, owned ? GREEN : 0xffb0bcac));
            ((TextView) cell.getChildAt(0)).setText(String.format(Locale.ROOT, "#%04d", id));
            ((TextView) cell.getChildAt(2)).setText(pokemon == null ? "???" : pokemon.title());
            ImageView image = (ImageView) cell.getChildAt(1);
            sprites.show(image, pokemon == null ? Pokemon.spriteUrl(id) : pokemon.sprite, owned);
            cell.setContentDescription(owned && pokemon != null ? pokemon.title() + ", capturado" : "Pokémon " + id + ", desconhecido");
            return cell;
        }
    }

    private void details(int id) {
        Pokemon pokemon = store.pokemon(id);
        if (pokemon == null) {
            new AlertDialog.Builder(this).setTitle("???")
                .setMessage("Pokémon #" + id + " ainda não capturado. Explore " + Regions.NAMES[Regions.of(id)] + " para encontrá-lo!")
                .setPositiveButton("EXPLORAR", (dialog, which) -> { store.region(Regions.of(id)); screen = 0; render(); loadEncounter(); })
                .setNegativeButton("VOLTAR", null).show();
            return;
        }
        LinearLayout content = column(); content.setPadding(dp(22), dp(12), dp(22), dp(16));
        ImageView sprite = new ImageView(this); sprites.show(sprite, pokemon.sprite, true);
        content.addView(sprite, new LinearLayout.LayoutParams(-1, dp(150)));
        JSONObject entry = store.entry(id);
        content.addView(label(pokemon.number() + " • " + pokemon.types, 13, GREEN));
        content.addView(label(String.format(Locale.getDefault(), "Altura: %.1f m   Peso: %.1f kg", pokemon.height / 10.0, pokemon.weight / 10.0), 13, INK));
        space(content, 12);
        content.addView(label("Primeira captura: " + date(entry.optLong("first")) + "\nÚltima captura: " + date(entry.optLong("last")) + "\nVezes capturado: " + entry.optInt("count"), 12, MUTED));
        space(content, 12);
        String[] statNames = {"HP", "Ataque", "Defesa", "Atq. especial", "Def. especial", "Velocidade"};
        for (int i = 0; i < 6; i++) content.addView(label(statNames[i] + ": " + pokemon.stats[i], 12, INK));
        ScrollView scroll = new ScrollView(this); scroll.addView(content);
        new AlertDialog.Builder(this).setTitle(pokemon.title()).setView(scroll).setPositiveButton("VOLTAR", null).show();
    }

    private void profile() {
        LinearLayout content = scrollContent();
        heading(content, "TREINADOR", "Cada captura faz parte da sua história.");
        LinearLayout trainer = card();
        ImageView avatar = new ImageView(this); avatar.setImageResource(R.drawable.ic_pokeball);
        trainer.addView(avatar, new LinearLayout.LayoutParams(dp(52), dp(52))); space(trainer, 8);
        TextView name = label(store.name(), 25, INK); name.setTypeface(Typeface.MONOSPACE, Typeface.BOLD); trainer.addView(name);
        trainer.addView(label("Treinador desde " + DateFormat.getDateInstance().format(store.created()), 12, MUTED));
        content.addView(trainer); space(content, 14);
        LinearLayout stats = row();
        stat(stats, String.valueOf(store.unique(0)), "ESPÉCIES");
        stat(stats, String.valueOf(store.total()), "CAPTURAS");
        stat(stats, String.format(Locale.getDefault(), "%.1f%%", store.unique(0) * 100.0 / Regions.TOTAL), "POKÉDEX");
        content.addView(stats); space(content, 22);
        TextView regions = label("SUAS REGIÕES", 11, INK); regions.setTypeface(pixel); content.addView(regions); space(content, 12);
        for (int r = 1; r < Regions.NAMES.length; r++) {
            final int region = r;
            LinearLayout regionCard = card(); regionCard.setPadding(dp(14), dp(12), dp(14), dp(12));
            regionCard.addView(label(Regions.NAMES[r] + "   " + store.unique(r) + " / " + Regions.size(r), 14, INK));
            ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            progress.setMax(Regions.size(r)); progress.setProgress(store.unique(r));
            regionCard.addView(progress, new LinearLayout.LayoutParams(-1, dp(12)));
            regionCard.setMinimumHeight(dp(60)); regionCard.setContentDescription("Abrir Pokédex de " + Regions.NAMES[r]);
            regionCard.setOnClickListener(v -> { dexRegion = region; search = ""; onlyCaught = false; screen = 1; render(); });
            content.addView(regionCard); space(content, 7);
        }
        space(content, 14); content.addView(label("ÚLTIMAS DESCOBERTAS", 12, INK));
        List<Integer> recent = store.recent();
        if (recent.isEmpty()) content.addView(label("Sua jornada começa com a primeira captura.", 13, MUTED));
        for (int id : recent) {
            Pokemon pokemon = store.pokemon(id); if (pokemon == null) continue;
            Button item = button(pokemon.title() + "  " + pokemon.number(), PAPER, INK); item.setTextSize(13);
            item.setOnClickListener(v -> details(id)); content.addView(item, fullButton()); space(content, 6);
        }
        space(content, 20);
        Button logout = button("SAIR DA CONTA", PAPER, RED); content.addView(logout, fullButton());
        logout.setOnClickListener(v -> {
            new AlertDialog.Builder(this).setTitle("Sair da conta?").setMessage("Suas capturas continuarão salvas neste aparelho.")
                .setNegativeButton("VOLTAR", null).setPositiveButton("SAIR", (dialog, which) -> {
                    session.edit().clear().apply(); requestToken++; store = null; current = null;
                    encounter = new Encounter(); loading = false; spawnError = null; screen = 0;
                    main.removeCallbacks(ticker); render();
                }).show();
        });
        space(content, 16); content.addView(label("Projeto educativo • Dados: PokéAPI\nPokémon pertence aos seus respectivos titulares.", 11, MUTED));
    }

    private void stat(LinearLayout row, String value, String title) {
        LinearLayout block = card(); block.setPadding(dp(8), dp(14), dp(8), dp(14)); block.setGravity(Gravity.CENTER);
        TextView number = label(value, 20, GREEN); number.setTypeface(Typeface.MONOSPACE, Typeface.BOLD); block.addView(number);
        block.addView(label(title, 9, MUTED));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -1, 1); params.setMargins(dp(3), 0, dp(3), 0);
        row.addView(block, params);
    }
    private LinearLayout scrollContent() {
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true);
        LinearLayout content = column(); content.setPadding(dp(18), dp(20), dp(18), dp(22)); scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1)); return content;
    }
    private Spinner regionSpinner(int selected) {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Regions.NAMES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter); spinner.setSelection(selected); spinner.setContentDescription("Selecionar região"); return spinner;
    }
    private void heading(LinearLayout parent, String title, String subtitle) {
        TextView heading = label(title, 17, INK); heading.setTypeface(pixel); parent.addView(heading); space(parent, 10);
        parent.addView(label(subtitle, 13, MUTED)); space(parent, 18);
    }
    private EditText input(String hint, boolean password) {
        EditText edit = new EditText(this); edit.setTextColor(INK); edit.setHintTextColor(MUTED); edit.setHint(hint);
        edit.setTextSize(14); edit.setSingleLine(true); edit.setPadding(dp(12), dp(10), dp(12), dp(10));
        edit.setBackground(border(PAPER, MUTED)); edit.setMinimumHeight(dp(52));
        edit.setInputType(password ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        edit.setTypeface(pixel);
        edit.setLayoutParams(new LinearLayout.LayoutParams(-1, -2)); return edit;
    }
    private Button button(String text, int background, int foreground) {
        Button button = new Button(this); button.setText(text); button.setTextColor(foreground); button.setTextSize(14);
        button.setTypeface(pixel); button.setAllCaps(false); button.setPadding(dp(10), dp(10), dp(10), dp(10));
        button.setMinHeight(dp(48)); button.setMinimumHeight(dp(48)); button.setBackground(border(background, INK)); return button;
    }
    private TextView label(String text, int size, int color) {
        TextView view = new TextView(this); view.setText(text); view.setTextSize(size); view.setTextColor(color);
        view.setTypeface(pixel); view.setLineSpacing(dp(3), 1); return view;
    }
    private LinearLayout column() { LinearLayout view = new LinearLayout(this); view.setOrientation(LinearLayout.VERTICAL); return view; }
    private LinearLayout row() { LinearLayout view = new LinearLayout(this); view.setOrientation(LinearLayout.HORIZONTAL); view.setGravity(Gravity.CENTER_VERTICAL); return view; }
    private LinearLayout card() {
        LinearLayout view = column(); view.setPadding(dp(16), dp(16), dp(16), dp(16)); view.setBackground(border(PAPER, INK)); return view;
    }
    private GradientDrawable border(int color, int stroke) {
        GradientDrawable drawable = new GradientDrawable(); drawable.setColor(color); drawable.setStroke(dp(2), stroke); return drawable;
    }
    private LinearLayout.LayoutParams fullButton() { return new LinearLayout.LayoutParams(-1, dp(54)); }
    private void space(LinearLayout parent, int height) { View view = new View(this); parent.addView(view, new LinearLayout.LayoutParams(1, dp(height))); }
    private int dp(int size) { return Math.round(size * getResources().getDisplayMetrics().density); }
    private String date(long millis) { return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(millis); }
    private void toast(String text) { Toast.makeText(this, text, Toast.LENGTH_SHORT).show(); }
}
