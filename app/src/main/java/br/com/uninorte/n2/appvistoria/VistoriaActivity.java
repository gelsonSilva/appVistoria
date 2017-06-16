package br.com.uninorte.n2.appvistoria;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

public class VistoriaActivity extends AppCompatPreferenceActivity {

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
//                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);

    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || WhatsAppFragment.class.getName().equals(fragmentName)
                || SendEmailFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), VistoriaActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     *
     * Envia o conteudo da saida da funcao gerarRelatorio para o whatsapp
     */

    public void whatsapp() {

        PackageManager pm=getPackageManager();
        try {

            Intent waIntent = new Intent(Intent.ACTION_SEND);
            waIntent.setType("text/plain");
            String text = gerarRelatorio();

            PackageInfo info=pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);

            waIntent.setPackage("com.whatsapp");

            waIntent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(waIntent, "Share with"));

        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "WhatsApp not Installed", Toast.LENGTH_SHORT)
                    .show();
        }

    }


    /**
     * Lista responsavel por gerenciar o menu de envio para o whatsapp
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class WhatsAppFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);

            ((VistoriaActivity)getActivity()).whatsapp();
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), VistoriaActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     *
     * @param key recebe a chave do item a ser retornado o valor
     * @param value valor do item no xml
     * @param context local de referencia dos dados
     */

    public static void putPrefString(String key, String value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     *
     * @param key recebe a chave do item no xml
     * @param value valor do item no xml
     * @param context local de referencia dos dados
     */

    public static void putPrefBoolean(String key, Boolean value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    /**
     *
     * @param valor numero do veiculo
     * @return string contendo o nome do veiculo selecionado
     */

    public static String retornaVeiculo(String valor){

        if (valor.contentEquals("1")){
            return "Moto";
        }
        else if (valor.contentEquals("2")){
            return "Carro";
        }
        else {
            return "Caminhonete";
        }
    }

    /**
     *
     * @param valor recebe um valor boleano
     * @return string contendo o resultado da comparacao
     */

    public static String converteValor(Boolean valor){

        if (valor){

            return "OK";
        }
        else {

            return "Falta";
        }

    }

    /**
     * Gera um relatorio contendo o resultado de todos os itens do xml
     *
     * @return retorna uma string com o resultado de todos os itens do xml
     */

    public String gerarRelatorio(){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        String relatorio = "";
        String editTextDataVistoria = preferences.getString("edit_text_data", "00/00/0000");
        String tipoVeiculo = preferences.getString("select_veiculo", "1");
        String editTextRG = preferences.getString("edit_text_rg", "Não informado");
        String editTextHorario = preferences.getString("edit_text_horario", "00:00");
        String motorista = preferences.getString("edit_text_motorista", "Não informado");
        String placa = preferences.getString("edit_text_placa", "???-0000");
        String hodometro = preferences.getString("edit_text_hodometro", "Não informado");
        String prefVTR = preferences.getString("edit_text_pref_vtr", "Não informado");
        String editTextObs = preferences.getString("edit_text_obs", "");

        Boolean oleoMotor = preferences.getBoolean("switch_oleo_motor", false);
        Boolean oleoFreio = preferences.getBoolean("switch_oleo_freio", false);
        Boolean oleoDirecao = preferences.getBoolean("switch_oleo_direcao", false);
        Boolean farol = preferences.getBoolean("switch_farol", false);
        Boolean pisca = preferences.getBoolean("switch_pisca", false);
        Boolean pneu = preferences.getBoolean("switch_pneu", false);
        Boolean estepe = preferences.getBoolean("switch_estepe", false);
        Boolean agua = preferences.getBoolean("switch_agua", false);
        Boolean chaveRoda = preferences.getBoolean("switch_chave_roda", false);
        Boolean macaco = preferences.getBoolean("switch_macaco", false);
        Boolean triangulo = preferences.getBoolean("switch_triangulo", false);
        Boolean extintor = preferences.getBoolean("switch_extintor", false);

        relatorio += "\nData da vistoria: " + editTextDataVistoria;
        relatorio += "\nTipo veículo: " + retornaVeiculo(tipoVeiculo);
        relatorio += "\nRG: " + editTextRG;
        relatorio += "\nHorário: " + editTextHorario;
        relatorio += "\nMotorista: " + motorista;
        relatorio += "\nPlaca: " + placa;
        relatorio += "\nHodometro: " + hodometro;
        relatorio += "\nPrefixo vtr: " + prefVTR;
        relatorio += "\nObservacoes: " + editTextObs;

        relatorio += "\nOleo do Motor: " + converteValor(oleoMotor);
        relatorio += "\nOleo do Freio: " + converteValor(oleoFreio);
        relatorio += "\nOleo do Direcao: " + converteValor(oleoDirecao);
        relatorio += "\nFarol: " + converteValor(farol);
        relatorio += "\nPisca: " + converteValor(pisca);
        relatorio += "\nPneu: " + converteValor(pneu);
        relatorio += "\nEstepe: " + converteValor(estepe);
        relatorio += "\nAgua do radiador: " + converteValor(agua);
        relatorio += "\nChave de Roda: " + converteValor(chaveRoda);
        relatorio += "\nMacaco: " + converteValor(macaco);
        relatorio += "\nTriangulo: " + converteValor(triangulo);
        relatorio += "\nExtintor: " + converteValor(extintor);

        putPrefString("edit_text_data", "00/00/0000", getApplicationContext());
        putPrefString("select_veiculo", "1", getApplicationContext());
        putPrefString("edit_text_rg", "0", getApplicationContext());
        putPrefString("edit_text_horario", "00:00", getApplicationContext());
        putPrefString("edit_text_motorista", "Não informado", getApplicationContext());
        putPrefString("edit_text_placa", "ZZZ-0000", getApplicationContext());
        putPrefString("edit_text_hodometro", "0", getApplicationContext());
        putPrefString("edit_text_pref_vtr", "0", getApplicationContext());
        putPrefString("edit_text_bos", "", getApplicationContext());

        putPrefBoolean("switch_oleo_motor", false, getApplicationContext());
        putPrefBoolean("switch_oleo_freio", false, getApplicationContext());
        putPrefBoolean("switch_oleo_direcao", false, getApplicationContext());
        putPrefBoolean("switch_farol", false, getApplicationContext());
        putPrefBoolean("switch_pisca", false, getApplicationContext());
        putPrefBoolean("switch_pneu", false, getApplicationContext());
        putPrefBoolean("switch_estepe", false, getApplicationContext());
        putPrefBoolean("switch_agua", false, getApplicationContext());
        putPrefBoolean("switch_chave_roda", false, getApplicationContext());
        putPrefBoolean("switch_macaco", false, getApplicationContext());
        putPrefBoolean("switch_triangulo", false, getApplicationContext());
        putPrefBoolean("switch_extintor", false, getApplicationContext());

        return relatorio;
    }

    public String tituloRelatorio(){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        String prefVTR = preferences.getString("edit_text_pref_vtr", "0");
        String placa = preferences.getString("edit_text_placa", "???-0000");

        return "Relatório Vistoria " + placa + "/" + prefVTR;
    }


    /**
     * Fragment responsavel por gerenciar o envio de emails
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SendEmailFragment extends PreferenceFragment {


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setHasOptionsMenu(true);

            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"contato@email.com"});
            i.putExtra(Intent.EXTRA_SUBJECT, ((VistoriaActivity)getActivity()).tituloRelatorio());
            i.putExtra(Intent.EXTRA_TEXT   , ((VistoriaActivity)getActivity()).gerarRelatorio());
            try {
                startActivity(Intent.createChooser(i, "Enviando email..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getActivity().getApplicationContext(), "Você não possui cliente de email instalado", Toast.LENGTH_SHORT).show();
            }

        }

    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ExitFragment extends PreferenceFragment {


        @SuppressLint("MissingSuperCall")
        public void onCreate(Bundle savedInstanceState) {


        }

    }
}
