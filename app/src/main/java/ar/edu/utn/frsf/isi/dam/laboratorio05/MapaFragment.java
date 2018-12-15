package ar.edu.utn.frsf.isi.dam.laboratorio05;


import android.Manifest;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.heatmaps.HeatmapTileProvider;


import java.util.ArrayList;
import java.util.List;

import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.MyDatabase;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.ReclamoDao;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapaFragment  extends SupportMapFragment implements OnMapReadyCallback {

    private static final int REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int LISTA_RECLAMOS = 2;
    private static final int RECLAMO = 3;
    private static final int HEATMAP = 4;
    private static final int TIPO_RECLAMO = 5;
    private static String[] PERMISSIONS_MAPS = {Manifest.permission.ACCESS_FINE_LOCATION};
    private GoogleMap miMapa;
    private int tipoMapa=0;
    private ReclamoDao reclamoDao;
    private List<Reclamo> listaReclamos;
    private Reclamo reclamo;


    private OnMapaListener listener;



    public interface OnMapaListener {
        public void obtenerCoordenadas();
        public void coordenadasSeleccionadas(LatLng c);
    }

    public void setListener(OnMapaListener listener) {
        this.listener = listener;
    }
    
    public MapaFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        listaReclamos = new ArrayList<>();
        reclamoDao = MyDatabase.getInstance(this.getActivity()).getReclamoDao();

        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        tipoMapa = 0;
        Bundle argumentos = getArguments();
        if(argumentos !=null) {
            tipoMapa = argumentos .getInt("tipo_mapa",0);
        }
        getMapAsync(this);
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap){
        miMapa = googleMap;
        UiSettings settings = miMapa.getUiSettings();
        settings.setZoomControlsEnabled(true);
        // Enabling MyLocation Layer of Google Map
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            try {
                miMapa.setMyLocationEnabled(true);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        else{
            verificarPermisoMapa(getActivity());
        }

        // Add a marker in Sydney and move the camera

        LatLng sydney = new LatLng(-34, 151);
        miMapa.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        miMapa.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        tipoMapaSeleccion(tipoMapa);
    }

    private void tipoMapaSeleccion(int tipoMapa) {
        switch (tipoMapa) {
            case 1:
                miMapa.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        listener.coordenadasSeleccionadas(latLng);
                    }
                });
                break;
            case 2:
                Runnable hiloCargarReclamos=new Runnable() {
                    @Override
                    public void run() {
                        listaReclamos.clear();
                        listaReclamos.addAll(reclamoDao.getAll());
                        Message completeMessage= handler.obtainMessage(LISTA_RECLAMOS);
                        completeMessage.sendToTarget();
                    }
                };
                Thread thread= new Thread(hiloCargarReclamos);
                thread.start();
                break;
            case 3:
                Runnable hiloCargarReclamo=(new Runnable() {
                    @Override
                    public void run() {
                        reclamo=reclamoDao.getById(getArguments().getInt("idReclamo"));
                        Message completeMessage= handler.obtainMessage(RECLAMO);
                        completeMessage.sendToTarget();
                    }
                });
                Thread thread2= new Thread(hiloCargarReclamo);
                thread2.start();
                break;
            case 4:
                Runnable hiloCargarReclamos2=new Runnable() {
                    @Override
                    public void run() {
                        listaReclamos.clear();
                        listaReclamos.addAll(reclamoDao.getAll());
                        Message completeMessage= handler.obtainMessage(HEATMAP);
                        completeMessage.sendToTarget();
                    }
                };
                Thread thread3= new Thread(hiloCargarReclamos2);
                thread3.start();
                break;
            case 5:
                Runnable tipoReclamos= new Runnable() {
                    @Override
                    public void run() {
                        listaReclamos.clear();
                        listaReclamos.addAll(reclamoDao.getByTipo(getArguments().getString("tipo_reclamo")));
                        if(!listaReclamos.isEmpty()){
                            Message completeMessage= handler.obtainMessage(TIPO_RECLAMO);
                            completeMessage.sendToTarget();}
                    }
                };
                Thread thread4= new Thread(tipoReclamos);
                thread4.start();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch ( requestCode ) {
            case REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    if (ContextCompat.checkSelfPermission(getContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED )
                        miMapa.setMyLocationEnabled(true);
                }
                break;
            }
        }
    }

    public static void verificarPermisoMapa(FragmentActivity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_MAPS,
                    REQUEST_ACCESS_FINE_LOCATION
            );
        }
    }


    Handler handler= new Handler(){
        @Override
        public void handleMessage(Message mensaje) {
            CameraUpdate cu;
            LatLng latLng;
            switch (mensaje.what){
                case LISTA_RECLAMOS:
                    ArrayList<MarkerOptions> marcadores= new ArrayList<>();
                    for(int i=0; listaReclamos.size()>i; i++){
                        latLng= new LatLng(listaReclamos.get(i).getLatitud(),
                                listaReclamos.get(i).getLongitud());
                        miMapa.addMarker(new MarkerOptions().position(latLng));
                        marcadores.add(new MarkerOptions().position(latLng));
                    }
                    LatLngBounds.Builder builder= new LatLngBounds.Builder();
                    for(MarkerOptions markerOptions: marcadores) builder.include(markerOptions.getPosition());
                    LatLngBounds bounds= builder.build();
                    cu= CameraUpdateFactory.newLatLngBounds(bounds, 0);
                    miMapa.moveCamera(cu);
                    break;
                case RECLAMO:
                    latLng= new LatLng(reclamo.getLatitud(), reclamo.getLongitud());
                    miMapa.addMarker(new MarkerOptions().position(latLng));
                    Circle circulo= miMapa.addCircle(new CircleOptions()
                            .center(latLng)
                            .radius(500)
                            .strokeColor(Color.RED)
                            .fillColor(0x220000FF)
                            .strokeWidth(5));
                    cu= CameraUpdateFactory.newLatLngZoom(latLng, 15.0f);
                    miMapa.moveCamera(cu);
                    break;
                case HEATMAP:
                    ArrayList<LatLng>listaCoordenadas = new ArrayList<>();
                    LatLngBounds.Builder builder2= new LatLngBounds.Builder();
                    for(int i=0; listaReclamos.size()>i; i++){
                        latLng= new LatLng(listaReclamos.get(i).getLatitud(),
                                listaReclamos.get(i).getLongitud());
                        listaCoordenadas.add(latLng);
                        builder2.include(latLng);
                    }

                    TileProvider mProvider = new HeatmapTileProvider
                            .Builder()
                            .data(listaCoordenadas)
                            .build();
                    miMapa.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                    LatLngBounds latLngBounds= builder2.build();
                    cu= CameraUpdateFactory.newLatLngBounds(latLngBounds, 0);
                    miMapa.moveCamera(cu);
                    break;
                case TIPO_RECLAMO:
                    ArrayList<LatLng> coordenadas= new ArrayList<>();
                    LatLngBounds.Builder builder3= new LatLngBounds.Builder();
                    PolylineOptions polOpt= new PolylineOptions();

                    for(int i=0; listaReclamos.size()>i; i++){
                        latLng= new LatLng(listaReclamos.get(i).getLatitud(), listaReclamos.get(i).getLongitud());
                        miMapa.addMarker(new MarkerOptions().position(latLng));
                        coordenadas.add(latLng);
                        builder3.include(latLng);
                        polOpt.add(latLng);
                    }
                    miMapa.addPolyline(polOpt.color(Color.RED).width(7));
                    LatLngBounds latLngBounds1= builder3.build();
                    cu= CameraUpdateFactory.newLatLngBounds(latLngBounds1, 0);
                    miMapa.moveCamera(cu);
                    break;
            }
        }
    };

}


