package com.example.projetws;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.projetws.beans.Etudiant;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String LOAD_URL = "http://10.0.2.2/projet/ws/loadEtudiant.php";
    private static final String UPDATE_URL = "http://10.0.2.2/projet/ws/updateEtudiant.php";
    private static final String DELETE_URL = "http://10.0.2.2/projet/ws/deleteEtudiant.php";

    private RequestQueue requestQueue;
    private EtudiantAdapter adapter;
    private final ArrayList<Etudiant> etudiants = new ArrayList<>();
    private ListView listEtudiants;
    private ProgressBar progress;
    private TextView empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestQueue = Volley.newRequestQueue(this);
        adapter = new EtudiantAdapter(this, etudiants);

        listEtudiants = findViewById(R.id.listEtudiants);
        progress = findViewById(R.id.progress);
        empty = findViewById(R.id.empty);
        Button openAdd = findViewById(R.id.openAdd);

        listEtudiants.setAdapter(adapter);
        listEtudiants.setOnItemClickListener((parent, view, position, id) ->
                afficherActions(etudiants.get(position)));
        openAdd.setOnClickListener(v -> startActivity(new Intent(this, AddEtudiant.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        chargerEtudiants();
    }

    private void chargerEtudiants() {
        progress.setVisibility(View.VISIBLE);
        empty.setVisibility(View.GONE);

        StringRequest request = new StringRequest(Request.Method.GET, LOAD_URL,
                response -> {
                    Log.d("RESPONSE", response);
                    Type type = new TypeToken<ArrayList<Etudiant>>() {
                    }.getType();
                    ArrayList<Etudiant> data = new Gson().fromJson(response, type);
                    etudiants.clear();
                    if (data != null) {
                        etudiants.addAll(data);
                    }
                    adapter.notifyDataSetChanged();
                    progress.setVisibility(View.GONE);
                    empty.setVisibility(etudiants.isEmpty() ? View.VISIBLE : View.GONE);
                },
                error -> {
                    progress.setVisibility(View.GONE);
                    Log.e("VOLLEY", "Erreur : " + error.toString());
                    Toast.makeText(this, "Impossible de charger la liste", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(request);
    }

    private void afficherActions(Etudiant etudiant) {
        String[] actions = {"Modifier", "Supprimer"};
        new AlertDialog.Builder(this)
                .setTitle(etudiant.getNom() + " " + etudiant.getPrenom())
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        afficherModification(etudiant);
                    } else {
                        confirmerSuppression(etudiant);
                    }
                })
                .show();
    }

    private void afficherModification(Etudiant etudiant) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, 0);

        EditText nomInput = new EditText(this);
        nomInput.setHint("Nom");
        nomInput.setText(etudiant.getNom());
        layout.addView(nomInput);

        EditText prenomInput = new EditText(this);
        prenomInput.setHint("Prenom");
        prenomInput.setText(etudiant.getPrenom());
        layout.addView(prenomInput);

        Spinner villeInput = new Spinner(this);
        ArrayAdapter<CharSequence> villeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.villes,
                android.R.layout.simple_spinner_item
        );
        villeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        villeInput.setAdapter(villeAdapter);
        int villePosition = villeAdapter.getPosition(etudiant.getVille());
        if (villePosition >= 0) {
            villeInput.setSelection(villePosition);
        }
        layout.addView(villeInput);

        RadioGroup sexeGroup = new RadioGroup(this);
        sexeGroup.setOrientation(RadioGroup.HORIZONTAL);
        RadioButton homme = new RadioButton(this);
        homme.setText("Homme");
        homme.setId(View.generateViewId());
        RadioButton femme = new RadioButton(this);
        femme.setText("Femme");
        femme.setId(View.generateViewId());
        sexeGroup.addView(homme);
        sexeGroup.addView(femme);
        sexeGroup.check("femme".equalsIgnoreCase(etudiant.getSexe()) ? femme.getId() : homme.getId());
        layout.addView(sexeGroup);

        new AlertDialog.Builder(this)
                .setTitle("Modifier")
                .setView(layout)
                .setNegativeButton("Annuler", null)
                .setPositiveButton("Enregistrer", (dialog, which) -> modifierEtudiant(
                        etudiant.getId(),
                        nomInput.getText().toString().trim(),
                        prenomInput.getText().toString().trim(),
                        villeInput.getSelectedItem().toString(),
                        sexeGroup.getCheckedRadioButtonId() == femme.getId() ? "femme" : "homme"
                ))
                .show();
    }

    private void modifierEtudiant(int id, String nom, String prenom, String ville, String sexe) {
        if (nom.isEmpty() || prenom.isEmpty()) {
            Toast.makeText(this, "Nom et prenom obligatoires", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST, UPDATE_URL,
                response -> {
                    Toast.makeText(this, "Etudiant modifie", Toast.LENGTH_SHORT).show();
                    appliquerListe(response);
                },
                error -> {
                    Log.e("VOLLEY", "Erreur : " + error.toString());
                    Toast.makeText(this, "Modification impossible", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(id));
                params.put("nom", nom);
                params.put("prenom", prenom);
                params.put("ville", ville);
                params.put("sexe", sexe);
                return params;
            }
        };
        requestQueue.add(request);
    }

    private void confirmerSuppression(Etudiant etudiant) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Supprimer " + etudiant.getNom() + " " + etudiant.getPrenom() + " ?")
                .setNegativeButton("Annuler", null)
                .setPositiveButton("Supprimer", (dialog, which) -> supprimerEtudiant(etudiant.getId()))
                .show();
    }

    private void supprimerEtudiant(int id) {
        StringRequest request = new StringRequest(Request.Method.POST, DELETE_URL,
                response -> {
                    Toast.makeText(this, "Etudiant supprime", Toast.LENGTH_SHORT).show();
                    appliquerListe(response);
                },
                error -> {
                    Log.e("VOLLEY", "Erreur : " + error.toString());
                    Toast.makeText(this, "Suppression impossible", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(id));
                return params;
            }
        };
        requestQueue.add(request);
    }

    private void appliquerListe(String response) {
        Type type = new TypeToken<ArrayList<Etudiant>>() {
        }.getType();
        ArrayList<Etudiant> data = new Gson().fromJson(response, type);
        etudiants.clear();
        if (data != null) {
            etudiants.addAll(data);
        }
        adapter.notifyDataSetChanged();
        empty.setVisibility(etudiants.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
