package ar.edu.utn.frsf.isi.dam.laboratorio05;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.MyDatabase;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.Reclamo;
import ar.edu.utn.frsf.isi.dam.laboratorio05.modelo.ReclamoDao;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.app.Activity.RESULT_OK;

public class NuevoReclamoFragment extends Fragment {

    public interface OnNuevoLugarListener {
        public void obtenerCoordenadas();
    }

    public void setListener(OnNuevoLugarListener listener) {
        this.listener = listener;
    }

    private Reclamo reclamoActual;
    private ReclamoDao reclamoDao;

    private EditText reclamoDesc;
    private EditText mail;
    private Spinner tipoReclamo;
    private TextView tvCoord;
    private Button buscarCoord;
    private Button btnGuardar;
    private Button btnFotoReclamo;
    private ImageView ivFotoReclamo;
    private Button btnGrabarAudio;
    private Button btnReproducirAudio;
    private Button btnDetenerGrabarAudio;

    private static final String LOG_TAG = "AudioRecordTest ";
    private MediaRecorder mRecorder = null ;
    private MediaPlayer mPlayer = null ;
    private String mFileName ;
    private Boolean grabando = false ;
    private Boolean reproduciendo = false ;

    private boolean tieneAudio,tieneFoto;

    File pathFoto = null;
    File pathAudio = null;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_SAVE = 2;

    private OnNuevoLugarListener listener;

    private ArrayAdapter<Reclamo.TipoReclamo> tipoReclamoAdapter;
    public NuevoReclamoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        reclamoDao = MyDatabase.getInstance(this.getActivity()).getReclamoDao();

        View v = inflater.inflate(R.layout.fragment_nuevo_reclamo, container, false);

        mFileName = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/audiorecordtest.3gp";

        View.OnClickListener listenerPlayer = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btnReproducirAudio:
                        if (reproduciendo) {
                            ((Button) view).setText("Reproducir");
                            reproduciendo = false;
                            terminarReproducir();
                        } else {
                            ((Button) view).setText("pausar.....");
                        }

                        reproduciendo = true;
                        reproducir();
                        break;
                    case R.id.btnAgregarAudio:
                        if (grabando) {
                            ((Button) view).setText("Grabar");
                            grabando = false;
                            terminarGrabar();
                        } else {
                            ((Button) view).setText("grabando.....");
                        }

                        grabando = true;

                        verificarSolicitarPermisoParaGrabar();

                        grabar();
                        break;
                }
            }
        };

        reclamoDesc = (EditText) v.findViewById(R.id.reclamo_desc);
        mail= (EditText) v.findViewById(R.id.reclamo_mail);
        tipoReclamo= (Spinner) v.findViewById(R.id.reclamo_tipo);
        tvCoord= (TextView) v.findViewById(R.id.reclamo_coord);
        buscarCoord= (Button) v.findViewById(R.id.btnBuscarCoordenadas);
        btnGuardar= (Button) v.findViewById(R.id.btnGuardar);
        btnFotoReclamo = (Button)v.findViewById(R.id.btnAgregarFoto);
        ivFotoReclamo = (ImageView) v.findViewById(R.id.ivFotoReclamo);
        btnGrabarAudio = (Button) v.findViewById(R.id.btnAgregarAudio);
        btnReproducirAudio = (Button) v.findViewById(R.id.btnReproducirAudio);
        btnDetenerGrabarAudio = (Button) v.findViewById(R.id.btnDetenerGrabarAudio);

        tipoReclamoAdapter = new ArrayAdapter<Reclamo.TipoReclamo>(getActivity(),android.R.layout.simple_spinner_item,Reclamo.TipoReclamo.values());
        tipoReclamoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipoReclamo.setAdapter(tipoReclamoAdapter);

        int idReclamo =0;
        if(getArguments()!=null)  {
            idReclamo = getArguments().getInt("idReclamo",0);
        }

        cargarReclamo(idReclamo);


        boolean edicionActivada = !tvCoord.getText().toString().equals("0;0");
        reclamoDesc.setEnabled(edicionActivada );
        mail.setEnabled(edicionActivada );
        tipoReclamo.setEnabled(edicionActivada);
        btnGuardar.setEnabled(edicionActivada);

        buscarCoord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.obtenerCoordenadas();

            }
        });

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveOrUpdateReclamo();
            }
        });

        btnFotoReclamo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {


                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    } else {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                225);
                    }


                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                            Manifest.permission.CAMERA)) {

                    } else {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.CAMERA},
                                226);
                    }
                } else {
                    sacarGuardarFoto();
                }
            }
        });

        btnGrabarAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.RECORD_AUDIO)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, 2);

                    } else {
                        grabar();
                    }
                }
            }
        });

        btnDetenerGrabarAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
                btnGrabarAudio.setEnabled(true);
                btnReproducirAudio.setEnabled(true);
                btnDetenerGrabarAudio.setEnabled(false);
                buscarCoord.setEnabled(true);

                tieneAudio = true;
                String tipo = tipoReclamo.getSelectedItem().toString();
                if(!tipo.equals(Reclamo.TipoReclamo.VEREDAS.toString()) && !tipo.equals(Reclamo.TipoReclamo.CALLE_EN_MAL_ESTADO.toString()))
                {
                    btnGuardar.setEnabled(true);
                }
            }
        });

        btnReproducirAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnGrabarAudio.setEnabled(false);
                btnDetenerGrabarAudio.setEnabled(false);
                btnReproducirAudio.setEnabled(true);
                buscarCoord.setEnabled(false);
                if(!reproduciendo){
                    reproduciendo = true;
                    mPlayer = new MediaPlayer();
                    try {
                        mPlayer.setDataSource(reclamoActual.getPathAudioReclamo());
                        mPlayer.prepare();
                        mPlayer.start();
                        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                mPlayer.release();
                                mPlayer = null;
                                reproduciendo = false;
                                btnReproducirAudio.setText("REPRODUCIR");
                                btnGrabarAudio.setEnabled(true);
                                btnDetenerGrabarAudio.setEnabled(false);
                                btnReproducirAudio.setEnabled(true);
                                buscarCoord.setEnabled(true);
                            }
                        });
                    } catch (IOException e) {
                        Log.e("AudioRecordTest", "prepare() failed");
                    }
                    btnReproducirAudio.setText("STOP");

                }else{
                    mPlayer.release();
                    mPlayer = null;
                    reproduciendo = false;
                    btnReproducirAudio.setText("REPRODUCIR");
                    btnGrabarAudio.setEnabled(true);
                    btnReproducirAudio.setEnabled(true);
                    btnDetenerGrabarAudio.setEnabled(false);
                    buscarCoord.setEnabled(true);
                }
            }
        });

        tipoReclamo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String tipo = parent.getItemAtPosition(position).toString();
                if((tipo.equals(Reclamo.TipoReclamo.VEREDAS.toString()) || tipo.equals(Reclamo.TipoReclamo.CALLE_EN_MAL_ESTADO.toString())) && tieneFoto)
                {
                    btnGuardar.setEnabled(true);
                }
                else if((reclamoDesc.getText().length()>=8 || tieneAudio) && !tipo.equals(Reclamo.TipoReclamo.VEREDAS.toString()) && !tipo.equals(Reclamo.TipoReclamo.CALLE_EN_MAL_ESTADO.toString()))
                {
                    btnGuardar.setEnabled(true);
                }
                else btnGuardar.setEnabled(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return v;
    }

    private void verificarSolicitarPermisoParaGrabar(){
        boolean permitido = true;

        if(Build.VERSION. SDK_INT >= Build.VERSION_CODES. M) {
            if (ContextCompat. checkSelfPermission (getContext() , RECORD_AUDIO ) != PackageManager. PERMISSION_GRANTED ) {
                permitido = false;
            }
        }

        if(permitido){
            if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), RECORD_AUDIO)){

            }else{
                ActivityCompat.requestPermissions(getActivity(), new String[]{RECORD_AUDIO}, 0);
            }
        }
    }

    private void grabar() {
        btnGrabarAudio.setEnabled(false);
        btnReproducirAudio.setEnabled(false);
        btnGuardar.setEnabled(false);
        btnDetenerGrabarAudio.setEnabled(true);
        buscarCoord.setEnabled(false);
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        try {
            createAudioFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRecorder.setOutputFile(reclamoActual.getPathAudioReclamo());
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e("AudioRecordTest", "prepare() failed");
        }
        mRecorder.start();
    }

    private void terminarGrabar () {
        mRecorder .stop ();
        mRecorder .release ();
        mRecorder = null ;
    }

    private void reproducir(){
        mPlayer = new MediaPlayer();
        try{
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        }catch (IOException e){
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void terminarReproducir(){
        mPlayer.release();
        mPlayer = null;
    }

    private void cargarReclamo(final int id) {
        if (id > 0) {
            Runnable hiloCargaDatos = new Runnable() {
                @Override
                public void run() {
                    reclamoActual = reclamoDao.getById(id);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(reclamoActual.getPathFotoReclamo()!=null) onActivityResult(REQUEST_IMAGE_SAVE, Activity.RESULT_OK, null);
                            if(reclamoActual.getPathAudioReclamo()!=null)
                            {
                                btnReproducirAudio.setEnabled(true);
                                tieneAudio=true;
                            }
                            pathFoto= new File(reclamoActual.getPathFotoReclamo());
                            pathAudio= new File(reclamoActual.getPathAudioReclamo());
                            mail.setText(reclamoActual.getEmail());
                            tvCoord.setText(reclamoActual.getLatitud() + ";" + reclamoActual.getLongitud());
                            reclamoDesc.setText(reclamoActual.getReclamo());
                            Reclamo.TipoReclamo[] tipos = Reclamo.TipoReclamo.values();
                            for (int i = 0; i < tipos.length; i++) {
                                if (tipos[i].equals(reclamoActual.getTipo())) {
                                    tipoReclamo.setSelection(i);
                                    break;
                                }
                            }
                        }
                    });
                }
            };
            Thread t1 = new Thread(hiloCargaDatos);
            t1.start();
        } else {
            String coordenadas = "0;0";
            if (getArguments() != null) coordenadas = getArguments().getString("latLng", "0;0");
            tvCoord.setText(coordenadas);
            reclamoActual = new Reclamo();
        }

    }

    private void saveOrUpdateReclamo(){
        reclamoActual.setEmail(mail.getText().toString());
        reclamoActual.setReclamo(reclamoDesc.getText().toString());
        reclamoActual.setTipo(tipoReclamoAdapter.getItem(tipoReclamo.getSelectedItemPosition()));
        if(tvCoord.getText().toString().length()>0 && tvCoord.getText().toString().contains(";")) {
            String[] coordenadas = tvCoord.getText().toString().split(";");
            reclamoActual.setLatitud(Double.valueOf(coordenadas[0]));
            reclamoActual.setLongitud(Double.valueOf(coordenadas[1]));
        }
        if(pathFoto != null){
            reclamoActual.setPathFotoReclamo(String.valueOf(pathFoto));
        }

        Runnable hiloActualizacion = new Runnable() {
            @Override
            public void run() {

                if((String.valueOf(pathFoto) != reclamoActual.getPathFotoReclamo()) && (String.valueOf(pathFoto) != null))
                {
                    File file = new File(String.valueOf(pathFoto));
                    if(file.delete()) System.out.println("Foto borrada");
                    else System.out.println("No se pudo borrar la foto");
                }
                if(String.valueOf(pathAudio)!=reclamoActual.getPathAudioReclamo() && String.valueOf(pathAudio)!=null)
                {
                    File file = new File(String.valueOf(pathAudio));
                    if(file.delete()) System.out.println("Audio borrado");
                    else System.out.println("No se pudo borrar el audio");
                }

                if(reclamoActual.getId()>0) reclamoDao.update(reclamoActual);
                else reclamoDao.insert(reclamoActual);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // limpiar vista
                        mail.setText(R.string.texto_vacio);
                        tvCoord.setText(R.string.texto_vacio);
                        reclamoDesc.setText(R.string.texto_vacio);
                        getActivity().getFragmentManager().popBackStack();
                    }
                });
            }
        };
        Thread t1 = new Thread(hiloActualizacion);
        t1.start();
    }

    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                                .format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File dir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                dir);

        pathFoto = image.getAbsoluteFile();

        return image;
    }

    private File createAudioFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        String audioFileName = "3GP_" + timeStamp + "_";
        File dir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File audio = File.createTempFile(
                audioFileName, /* prefix */
                ".3gp", /* suffix */
                dir /* directory */
        );
        reclamoActual.setPathAudioReclamo(audio.getAbsolutePath());
        return audio;
    }

    private void sacarGuardarFoto(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null){
            File photoFile = null;
            try {
                photoFile = createImageFile();
            }catch (IOException ex){
                ex.printStackTrace();
            }

            if (photoFile != null){
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            File file = new File(pathFoto.getAbsolutePath());
            Bitmap imageBitmap = null;
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Uri.fromFile(file));
            } catch (IOException e) {
                e.printStackTrace();
            }

            ivFotoReclamo.setImageBitmap(imageBitmap);
        }
        if (requestCode == REQUEST_IMAGE_SAVE && resultCode == Activity.RESULT_OK) {
            Bitmap imageBitmap = null;
            try {
                File file = new File(reclamoActual.getPathFotoReclamo());
                imageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Uri.fromFile(file));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException n){
                n.printStackTrace();
            }
            if (imageBitmap != null) {
                ivFotoReclamo.setImageBitmap(imageBitmap);
                tieneFoto=true;
                String tipo = tipoReclamo.getSelectedItem().toString();
                if(tipo.equals(Reclamo.TipoReclamo.VEREDAS.toString()) || tipo.equals(Reclamo.TipoReclamo.CALLE_EN_MAL_ESTADO.toString()))
                {
                    btnGuardar.setEnabled(true);
                }
            }

        }
    }
}
