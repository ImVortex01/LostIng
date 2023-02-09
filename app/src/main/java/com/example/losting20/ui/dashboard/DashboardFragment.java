package com.example.losting20.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.losting20.Incidencia;
import com.example.losting20.SharedViewModel;
import com.example.losting20.databinding.FragmentDashboardBinding;
import com.example.losting20.databinding.NotificationsRowBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private FirebaseUser authUser;
    private String imagen = "https://firebasestorage.googleapis.com/v0/b/losting2-0.appspot.com/o/publicaciones%2F527073d7-b298-46b6-8efa-0c9a9243e5f7?alt=media&token=";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);
        SharedViewModel sharedViewModel = new ViewModelProvider(
                requireActivity()
        ).get(SharedViewModel.class);


        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        sharedViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            authUser = user;

            if (user != null) {
                DatabaseReference base = FirebaseDatabase.getInstance().getReference();

                DatabaseReference users = base.child("users");
                DatabaseReference uid = users.child(authUser.getUid());
                DatabaseReference incidencies = uid.child("reportes");

                FirebaseRecyclerOptions<Incidencia> options = new FirebaseRecyclerOptions.Builder<Incidencia>()
                        .setQuery(incidencies, Incidencia.class)
                        .setLifecycleOwner(this)
                        .build();

                IncidenciaAdapter adapter = new IncidenciaAdapter(options);

                binding.rvIncidencies.setAdapter(adapter);
                binding.rvIncidencies.setLayoutManager(new LinearLayoutManager(requireContext())
                );
                return;
            }
        });
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    class IncidenciaAdapter extends FirebaseRecyclerAdapter<Incidencia, IncidenciaAdapter.IncidenciaViewholder> {
        public IncidenciaAdapter(@NonNull FirebaseRecyclerOptions<Incidencia> options) {
            super(options);
        }

        @Override
        protected void onBindViewHolder(
                @NonNull IncidenciaViewholder holder, int position, @NonNull Incidencia model
        ) {
            holder.binding.txtDescripcio.setText(model.getProblema());
            holder.binding.txtAdreca.setText(model.getDireccio());
            try{
                Glide.with(getContext()).load(imagen + model.getUrl().toString()).into(holder.binding.ivArticulo);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @NonNull
        @Override
        public IncidenciaViewholder onCreateViewHolder(
                @NonNull ViewGroup parent, int viewType
        ) {
            return new IncidenciaViewholder(NotificationsRowBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent, false));
        }

        class IncidenciaViewholder extends RecyclerView.ViewHolder {
            NotificationsRowBinding binding;

            public IncidenciaViewholder(NotificationsRowBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}