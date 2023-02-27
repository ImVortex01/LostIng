package com.example.losting20.ui.dashboard;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.losting20.SharedViewModel;
import com.example.losting20.databinding.CentrosRowBinding;
import com.example.losting20.databinding.FragmentDashboardBinding;
import com.example.losting20.ui.Centros;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private FirebaseUser authUser;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        sharedViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            authUser = user;

            if (user != null) {
                DatabaseReference base = FirebaseDatabase.getInstance().getReference();

                DatabaseReference users = base.child("users");
                DatabaseReference uid = users.child(authUser.getUid());
                DatabaseReference centros = uid.child("Centros");

                FirebaseRecyclerOptions<Centros> options = new FirebaseRecyclerOptions.Builder<Centros>()
                        .setQuery(centros, Centros.class)
                        .setLifecycleOwner(this)
                        .build();

                CentroAdapter adapter = new CentroAdapter(options);

                binding.lvCentros.setAdapter(adapter);
                binding.lvCentros.setLayoutManager(new LinearLayoutManager(requireContext()));

            }

               /* DatabaseReference base = FirebaseDatabase.getInstance(
                ).getReference();

                Centros centros = new Centros();
                centros.setDireccio("Gran Vía del Marqués del Turia, 58, 8");
                centros.setLatitud("39.467462091614586");
                centros.setLongitud("-0.36750803491229983");

                DatabaseReference users = base.child("users");
                DatabaseReference uid = users.child(authUser.getUid());
                DatabaseReference incidencies = uid.child("Centros");

                DatabaseReference reference = incidencies.push();
                reference.setValue(centros);
*/
        });
        return root;
    }

    public class CentroAdapter extends FirebaseRecyclerAdapter<Centros, DashboardFragment.CentroAdapter.CentroViewHolder> implements ListAdapter {
        public CentroAdapter(@NonNull FirebaseRecyclerOptions<Centros> options) {
            super(options);
        }

        @Override
        protected void onBindViewHolder(@NonNull CentroViewHolder holder, int position, @NonNull Centros model) {
            holder.binding.txtDescripcion.setText(model.getDireccio());
        }

        @NonNull
        @Override
        public DashboardFragment.CentroAdapter.CentroViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent, int viewType
        ) {
            return new CentroViewHolder(CentrosRowBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent, false));
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int i) {
            return false;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver dataSetObserver) {

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

        }

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        public class CentroViewHolder extends RecyclerView.ViewHolder {
            public CentrosRowBinding binding;

            public CentroViewHolder(CentrosRowBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}