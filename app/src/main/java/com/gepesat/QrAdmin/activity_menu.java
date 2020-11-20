package com.gepesat.QrAdmin;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class activity_menu extends AppCompatActivity {
    Variables_Globales va=Variables_Globales.getInstance();
    private static  final int REQUEST_LOCATION=1;
    static String cadenaConexion = "jdbc:postgresql://gepesat-postgresql-do-user-7581876-0.a.db.ondigitalocean.com:25060/defaultdb?sslfactory=org.postgresql.ssl.NonValidatingFactory&ssl=true";
    private static clsConexionCP con2=new clsConexionCP();

    //igualmente declaramos los elementos
    TextView txtdatos,txtqr,txtemail,txttelefono;
    private Spinner spinnerZ, spinnerS, spinnerU, spinnerC, spinnerM, spinnerT;
    private IntentIntegrator qrScan;
    ArrayList<String> zona;
    ArrayList<String> sector;
    ArrayList<String> ubicacion;
    ArrayList<String> color;
    ArrayList<String> marca;

    Connection conexxion=null;

    LocationManager locationManager;
    String latitude,longitude, codigo, cadena, opcion, fecha, spZ, spS, spU, spC, spM, spT, qrcodigo, t;
    //private String qrcodigo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        //Add permission GPS

        ActivityCompat.requestPermissions(this,new String[]
                {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        //txtdatos=findViewById(R.id.txtdatos);
        txtqr   =findViewById(R.id.txtqr);

        //if(va.get_codigousuario()!=0){
        //    txtdatos.setText(va.get_datos().toString());
        //    codigo = Integer.toString(va.get_codigousuario());
        //}
        //txtqr.setText("QR LEIDO");
        //txtqr.setVisibility(View.GONE);

        spinnerZ = (Spinner) findViewById(R.id.tipo_spinnerZ);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterZ = ArrayAdapter.createFromResource(this,
                R.array.tipo_zona, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterZ.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerZ.setAdapter(adapterZ);

        spinnerS = (Spinner) findViewById(R.id.tipo_spinnerS);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterS = ArrayAdapter.createFromResource(this,
                R.array.tipo_sector, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterS.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerS.setAdapter(adapterS);

        spinnerU = (Spinner) findViewById(R.id.tipo_spinnerU);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterU = ArrayAdapter.createFromResource(this,
                R.array.tipo_ubicacion, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterU.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerU.setAdapter(adapterU);

        spinnerM = (Spinner) findViewById(R.id.tipo_spinnerM);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterM = ArrayAdapter.createFromResource(this,
                R.array.tipo_marca, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterM.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerM.setAdapter(adapterM);

        spinnerC = (Spinner) findViewById(R.id.tipo_spinnerC);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterC = ArrayAdapter.createFromResource(this,
                R.array.tipo_color, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterC.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerC.setAdapter(adapterC);

        spinnerT = (Spinner) findViewById(R.id.tipo_spinnerT);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterT = ArrayAdapter.createFromResource(this,
                R.array.tipo_tipo, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterT.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerT.setAdapter(adapterT);

        //Spinner spZ = (Spinner) findViewById(R.id.tipo_spinnerZ);
        //listar();
        //spZ.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        //    @Override
        //    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
         //       String zona=   spinnerZ.getItemAtPosition(spinnerZ.getSelectedItemPosition()).toString();
          //      Toast.makeText(getApplicationContext(),zona,Toast.LENGTH_LONG).show();
         //   }
         //   @Override
         //   public void onNothingSelected(AdapterView<?> adapterView) {
         //       // DO Nothing here
         //   }
        //});

        //Declaramos los botones

        Button btn1 = (Button) findViewById(R.id.button1);
        Button btn3 = (Button) findViewById(R.id.button3);
        Button btn4 = (Button) findViewById(R.id.button4);
        Button btn5 = (Button) findViewById(R.id.button5);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast notificacion = Toast.makeText(activity_menu.this, "Esto es el botón", Toast.LENGTH_SHORT);
                //notificacion.show();
                //switch (v.getId()){
                //    case R.id.btnScanner:
                        ///new IntentIntegrator(activity_menu.this).initiateScan();
                qrScan = new IntentIntegrator(activity_menu.this);
                qrScan.setOrientationLocked(false);
                qrScan.initiateScan();
                //       break;
                //}

            }
        });



        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("myQR", Activity.MODE_PRIVATE);
                qrcodigo = sharedPreferences.getString("myQRKey","unknown");

                if (qrcodigo=="" || qrcodigo==null) {
                    Toast.makeText(getApplicationContext(), "QR no puede esta VACIO", Toast.LENGTH_SHORT).show();
                } else {
                    locationManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    //Check gps is enable or not
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    {
                        //Write Function To enable gps
                        OnGPS();
                    }
                    else
                    {
                        //GPS is already On then
                        if (grabar_770()==false) {
                            Toast notificacion = Toast.makeText(activity_menu.this, "No grabó", Toast.LENGTH_SHORT);
                            notificacion.show();
                        }
                    }
                }
            }


        });
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Verificar();
            }
        });

        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Actualizar();
            }
        });

    }

    //@Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null)
            if (result.getContents() != null){
                qrcodigo = result.getContents();
                SharedPreferences sharedPreferences = getSharedPreferences("myQR", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("myQRKey", qrcodigo);
                editor.commit();
                txtqr.post(new Runnable() {
                    @Override
                    public void run() {
                        txtqr.setText(qrcodigo);
                    }
                });
                //txtqr.setText(qrcodigo);

                //txtqr.setVisibility(View.VISIBLE);
                //runOnUiThread(new Runnable() {
                //    @Override
                //    public void run() {
                //        txtqr.setText(qrcode);
                //    }
                //});


                Toast notificacionqr = Toast.makeText(activity_menu.this, qrcodigo, Toast.LENGTH_SHORT);
                notificacionqr.show();

            }else{
                Toast notificacionqr = Toast.makeText(activity_menu.this, "Error al escanear QR", Toast.LENGTH_SHORT);
                notificacionqr.show();
            }
    }
    public void listar(){
        RequestQueue requestQueue= Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest=new StringRequest(Request.Method.POST, Conexion.URL_WEB_SERVICES+"zona-listar.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonObject=new JSONObject(response);
                            JSONArray jsonArray=jsonObject.getJSONArray("zona");
                            for(int i=0;i<jsonArray.length();i++){
                                JSONObject jsonObject1=jsonArray.getJSONObject(i);
                                String country=jsonObject1.getString("descripcion");
                                zona.add(country);
                            }
                            spinnerZ.setAdapter(new ArrayAdapter<String>(activity_menu.this, android.R.layout.simple_spinner_dropdown_item, zona));
                        }catch (JSONException e){e.printStackTrace();}
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        requestQueue.add(stringRequest);
    }
    //public boolean VerificaRed (Context context) {
    public boolean VerificaRed () {
        //ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        //return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
        //AsyncTask<Boolean> ok = new CheckInternetAsyncTask(getApplicationContext()).execute();
        // ok;
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            try {
                URL url = new URL("http://www.google.com");
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setConnectTimeout(3000);
                urlc.connect();
                if (urlc.getResponseCode() == 200) {
                    return new Boolean(true);
                }
            } catch (MalformedURLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return false;
    }
    public boolean grabar_770() {
        SharedPreferences sharedPreferences = getSharedPreferences("myQR", Activity.MODE_PRIVATE);
        qrcodigo = sharedPreferences.getString("myQRKey","unknown");

        if (qrcodigo=="" || qrcodigo==null) {
            Toast.makeText(getApplicationContext(), "QR no puede esta VACIO", Toast.LENGTH_SHORT).show();
        } else {
            Toast notificacionrec = Toast.makeText(activity_menu.this, "En grabacion", Toast.LENGTH_SHORT);
            notificacionrec.show();
            codigo = Integer.toString(va.get_codigousuario());
            getLocation();
            Date date = new Date();

            DateFormat FechaCompleta = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            fecha = FechaCompleta.format(date);
            spZ = spinnerZ.getSelectedItem().toString();
            spS = spinnerS.getSelectedItem().toString();
            spU = spinnerU.getSelectedItem().toString();
            spC = spinnerC.getSelectedItem().toString();
            spM = spinnerM.getSelectedItem().toString();
            spT = spinnerT.getSelectedItem().toString();
            opcion = "0";
            //qrcode = qrScan. // ;String contents = intent.getStringExtra("SCAN_RESULT");
            //qrcode = txtqr.getText().toString();
            cadena = "(" + codigo + ";" + opcion + ";" + latitude + ";" + longitude + ";" + qrcodigo + ";" + fecha + ";" + spZ + "," + spS + "," + spU + "," + spC + "," + spM + "," + spT + ")";
            //clsConexionCP con=new clsConexionCP();
            //if (VerificaRed(getApplicationContext())) {
            if (isOnline() && isNetworkAvailable()) {
                //FileInputStream fis = null;
                try {

                    //String storeProcedureCall="{CALL pa_logueo_android(?,?,?,?,?)}";
                    //CallableStatement cStmt=
                    String ruta = "";

                    //File file = new File(ruta);
                    //fis = new FileInputStream(file);
                    PreparedStatement cStmt = con2.conexionBD2().prepareStatement("INSERT into GQR770(port_in,port_out,datos) VALUES (?,?,?)");
                    //Statement cStmt = (Statement) con2.conexionBD2();
                    //Estos dos primeros parametros son los de entrada

                    cStmt.setString(1, codigo);
                    cStmt.setString(2, "27701");
                    cStmt.setString(3, cadena);

                    cStmt.execute();
                    cStmt.close();
                    Toast notificacion = Toast.makeText(activity_menu.this, "GRABACION ENVIADA", Toast.LENGTH_SHORT);
                    notificacion.show();
                    //SharedPreferences sharedPreferences = getSharedPreferences("myQR", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("myQRKey", "");
                    editor.commit();
                    txtqr.post(new Runnable() {
                        @Override
                        public void run() {
                            txtqr.setText("");
                        }
                    });
                    return true;
                    //} catch (FileNotFoundException e) {
                    //    System.out.println(e.getMessage());
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                    //} finally {
                    //try {
                    //fis.close();
                    //} catch (IOException e) {
                    //    System.out.println(e.getMessage());
                    //}
                }
            } else {
                SQLiteDatabase myDB = openOrCreateDatabase("my.db", MODE_PRIVATE, null);
                myDB.execSQL("INSERT INTO gqr770 (port_in,port_out,datos) VALUES ("+codigo+",'27701','"+cadena+"') ");
                Toast notificacion = Toast.makeText(activity_menu.this, "GRABACION POR ENVIAR", Toast.LENGTH_SHORT);
                notificacion.show();
                //SharedPreferences sharedPreferences = getSharedPreferences("myQR", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("myQRKey", "");
                editor.commit();
                txtqr.post(new Runnable() {
                    @Override
                    public void run() {
                        txtqr.setText("");
                    }
                });
                return true;
            }
        }

        return false;
    }

    private void getLocation() {

        //Check Permissions again

        if (ActivityCompat.checkSelfPermission(activity_menu.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity_menu.this,

                Manifest.permission.ACCESS_COARSE_LOCATION) !=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
        else
        {
            Location LocationGps= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location LocationNetwork=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location LocationPassive=locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (LocationGps !=null)
            {
                double lat=LocationGps.getLatitude();
                double longi=LocationGps.getLongitude();

                latitude=String.valueOf(lat);
                longitude=String.valueOf(longi);

                //showLocationTxt.setText("Your Location:"+"\n"+"Latitude= "+latitude+"\n"+"Longitude= "+longitude);

                Toast notificacion = Toast.makeText(activity_menu.this, "Gps Latitude= "+latitude+" "+"Longitude= "+longitude, Toast.LENGTH_SHORT);
                notificacion.show();
            }
            else if (LocationNetwork !=null)
            {
                double lat=LocationNetwork.getLatitude();
                double longi=LocationNetwork.getLongitude();

                latitude=String.valueOf(lat);
                longitude=String.valueOf(longi);

                //showLocationTxt.setText("Your Location:"+"\n"+"Latitude= "+latitude+"\n"+"Longitude= "+longitude);
                Toast notificacion = Toast.makeText(activity_menu.this, "Red Latitude= "+latitude+" "+"Longitude= "+longitude, Toast.LENGTH_SHORT);
                notificacion.show();
            }
            else if (LocationPassive !=null)
            {
                double lat=LocationPassive.getLatitude();
                double longi=LocationPassive.getLongitude();

                latitude=String.valueOf(lat);
                longitude=String.valueOf(longi);

                //showLocationTxt.setText("Your Location:"+"\n"+"Latitude= "+latitude+"\n"+"Longitude= "+longitude);
                Toast notificacion = Toast.makeText(activity_menu.this, "Pas Latitude= "+latitude+" "+"Longitude= "+longitude, Toast.LENGTH_SHORT);
                notificacion.show();
            }
            else
            {
                Toast.makeText(this, "Can't Get Your Location", Toast.LENGTH_SHORT).show();
            }
            
            //Thats All Run Your App
        }

    }

    private void OnGPS() {

        final AlertDialog.Builder builder= new AlertDialog.Builder(this);

        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });
        final AlertDialog alertDialog=builder.create();
        alertDialog.show();
    }

    private void Verificar() {

        SQLiteDatabase myDB = openOrCreateDatabase("my.db", MODE_PRIVATE, null);

        String countQuery = "SELECT  * FROM gqr770";
        //SQLiteDatabase myDB = this.getReadableDatabase();
        Cursor cursor =myDB.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        Toast notificacionV = Toast.makeText(activity_menu.this, "POR ENVIAR: "+count, Toast.LENGTH_SHORT);
        notificacionV.show();
    }

    public void Actualizar(){

        Statement sentencia = null;
        ResultSet resultado = null;
        if (isOnline() && isNetworkAvailable()) {
            try {
                Toast.makeText(getApplicationContext(), "ACTUALIZANDO", Toast.LENGTH_SHORT).show();
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                Class.forName("org.postgresql.Driver");
                conexxion = DriverManager.getConnection(cadenaConexion,"doadmin","fmawbjj6kmdbc330");
                sentencia = conexxion.createStatement();
                String consultaSQL = "SELECT id_conductor, nom_conductor, email_coductor, clave_conductor FROM gpn_conductor WHERE id_user='404' ";
                resultado = sentencia.executeQuery(consultaSQL);

                //abriendo interna para actualizar usuarios
                SQLiteDatabase myDB = openOrCreateDatabase("my.db", MODE_PRIVATE, null);
                myDB.execSQL("DROP TABLE IF EXISTS gpn_conductor");
                myDB.execSQL("CREATE TABLE gpn_conductor (id_conductor INTEGER, nom_conductor VARCHAR(50), email_coductor VARCHAR(50), clave_conductor VARCHAR(20))");
                while (resultado.next()) {
                    int id = resultado.getInt("id_conductor");
                    String Nombre = resultado.getString("nom_conductor");
                    String user = resultado.getString("email_coductor");
                    String pass = resultado.getString("clave_conductor");
                    myDB.execSQL("INSERT INTO gpn_conductor (id_conductor,nom_conductor,email_coductor,clave_conductor) VALUES ("+id+",'"+Nombre+"','"+user+"','"+pass+"') ");
                    //respuestaSql = respuestaSql + id + " | " + Nombre +  "\n";
                }
                //Toast.makeText(getApplicationContext(), respuestaSql, Toast.LENGTH_SHORT).show();
                //actualizando los datos en el celular
                myDB.execSQL("CREATE TABLE IF NOT EXISTS gqr770 (id INTEGER, port_in VARCHAR(50), port_out VARCHAR(50), datos VARCHAR(300))");
                Cursor myCursor = myDB.rawQuery("SELECT * FROM gqr770", null);

                String selected = "";

                if (myCursor != null && myCursor.moveToFirst()) {

                    while (!myCursor.isAfterLast()) {

                        //get items at selected position
                        //selected = column.get(where);

                        Integer idqr = myCursor.getInt(0);
                        String  inqr = myCursor.getString(1);
                        String  outqr= myCursor.getString(2);
                        String  datqr= myCursor.getString(3);

                        //qr770
                        PreparedStatement cStmt = con2.conexionBD2().prepareStatement("INSERT into GQR770(port_in,port_out,datos) VALUES (?,?,?)");
                        //Statement cStmt = (Statement) con2.conexionBD2();
                        //Estos dos primeros parametros son los de entrada

                        cStmt.setString(1, inqr);
                        cStmt.setString(2, "27701");
                        cStmt.setString(3, datqr);

                        cStmt.execute();
                        cStmt.close();
                        //consultaSQL = "INSERT INTO gqr770 (port_in,port_out,datos) VALUES ('"+inqr+"','"+outqr+"','"+datqr+"')";
                        //resultado = sentencia.executeQuery(consultaSQL);
                        //while (resultado.next()) {
                        myDB.execSQL("DELETE FROM gqr770 WHERE datos='"+datqr+"'");
                        //}
                        myCursor.moveToNext();
                    }
                    myCursor.close();
                    Toast.makeText(getApplicationContext(), "ACTUALIZACION FINALIZADA", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "NO HAY DATOS POR ACTUALIZAR", Toast.LENGTH_SHORT).show();
                }
                myDB.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(e.getMessage());
                System.err.println("Error: Cant connect!");
                conexxion = null;
            } finally {
                if (resultado != null) {
                    try {
                        resultado.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(e.getMessage());
                    }
                }
                if (sentencia != null) {
                    try {
                        sentencia.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(e.getMessage());
                    }
                }
                if (conexxion != null) {
                    try {
                        conexxion.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(e.getMessage());
                    }
                }
            }
            System.err.println("----- PostgreSQL query ends correctly!-----");
            //return null;
        } else {
            Toast.makeText(getApplicationContext(), "NO HAY RED ACTIVA", Toast.LENGTH_SHORT).show();
        }
    }

    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();}


    public Boolean isOnline() {
        try {
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
            int returnVal = p1.waitFor();
            boolean reachable = (returnVal==0);
            return reachable;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
    @Override
    protected void onSaveInstanceState(Bundle guardaEstado) {
        super.onSaveInstanceState(guardaEstado);
        //guardamos en la variable t el texto del campo EditText
        t = txtqr.getText().toString();
        //lo "guardamos" en el Bundle
        guardaEstado.putString("text", t);
    }

    @Override
    protected void onRestoreInstanceState(Bundle recuperaEstado) {
        super.onRestoreInstanceState(recuperaEstado);
        //recuperamos el String del Bundle
        t = recuperaEstado.getString("text");
        //Seteamos el valor del EditText con el valor de nuestra cadena
        txtqr.setText(t);
    }
}
