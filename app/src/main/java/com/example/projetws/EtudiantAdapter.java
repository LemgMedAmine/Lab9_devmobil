package com.example.projetws;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.projetws.beans.Etudiant;

import java.util.List;

public class EtudiantAdapter extends ArrayAdapter<Etudiant> {

    public EtudiantAdapter(@NonNull Context context, @NonNull List<Etudiant> etudiants) {
        super(context, 0, etudiants);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_etudiant, parent, false);
        }

        Etudiant etudiant = getItem(position);
        TextView name = view.findViewById(R.id.name);
        TextView details = view.findViewById(R.id.details);

        if (etudiant != null) {
            name.setText(etudiant.getNom() + " " + etudiant.getPrenom());
            details.setText(etudiant.getVille() + " - " + etudiant.getSexe());
        }

        return view;
    }
}
