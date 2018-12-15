package ar.edu.utn.frsf.isi.dam.laboratorio05;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;


/**
 * A simple {@link Fragment} subclass.
 */
public class FormularioBuscarFragment extends Fragment {

    private Button btnBuscar;
    private Spinner spTipoReclamo;
    private OnFormularioBuscarListener listener;
    private ArrayAdapter tipoReclamoAdapter;


    public interface OnFormularioBuscarListener{
        public void buscarReclamo(String tipo);
    }

    public void setListener (OnFormularioBuscarListener listener){ this.listener=listener;}



    public FormularioBuscarFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view= inflater.inflate(R.layout.fragment_formulario_buscar, container, false);

        btnBuscar= (Button) view.findViewById(R.id.btnBuscar);
        spTipoReclamo = (Spinner) view.findViewById(R.id.spinnerTipos);

        List<String> tiposReclamos= listaTipoReclamos();
        tipoReclamoAdapter= new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, tiposReclamos);
        spTipoReclamo.setAdapter(tipoReclamoAdapter);

        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.buscarReclamo(spTipoReclamo.getSelectedItem().toString());
            }
        });
        return view;
    }


    private List<String> listaTipoReclamos(){
        List<String> listaString= new ArrayList<>();
        List<Reclamo.TipoReclamo> lista= Arrays.asList(Reclamo.TipoReclamo.values());
        for (int i=0; lista.size()>i; i++){
            listaString.add(lista.get(i).toString());
        }
        return  listaString;
    }

}
