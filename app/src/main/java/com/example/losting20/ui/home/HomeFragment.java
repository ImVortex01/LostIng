package com.example.losting20.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.losting20.Incidencia;
import com.example.losting20.R;
import com.example.losting20.SharedViewModel;
import com.example.losting20.databinding.FragmentHomeBinding;
import com.example.losting20.databinding.LvArticulosBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FirebaseUser authUser;
    private final String imagen = "https://firebasestorage.googleapis.com/v0/b/losting2-0.appspot.com/o/publicaciones%2F";

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        binding.addReppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(HomeFragment.this).navigate(R.id.action_navigation_home_to_addReport);
            }
        });

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

                binding.lvArticulos.setAdapter(adapter);
                binding.lvArticulos.setLayoutManager(
                        new GridLayoutManager(requireContext(), 3)
                );
                return;
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public class IncidenciaAdapter extends FirebaseRecyclerAdapter<Incidencia, IncidenciaAdapter.IncidenciaViewholder> {
        public IncidenciaAdapter(@NonNull FirebaseRecyclerOptions<Incidencia> options) {
            super(options);
        }

        @Override
        protected void onBindViewHolder(
                @NonNull IncidenciaViewholder holder, int position, @NonNull Incidencia model
        ) {
            holder.binding.tvArticulo.setText(model.getProblema());
            try {
                Glide.with(getContext()).load(imagen + model.getUrl() + "?alt=media&token=" + model.getUrl()).into(holder.binding.ivarticulos2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @NonNull
        @Override
        public IncidenciaViewholder onCreateViewHolder(
                @NonNull ViewGroup parent, int viewType
        ) {
            return new IncidenciaAdapter.IncidenciaViewholder(LvArticulosBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent, false));
        }

        public class IncidenciaViewholder extends RecyclerView.ViewHolder {
            public LvArticulosBinding binding;

            public IncidenciaViewholder(LvArticulosBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}