package com.gepesat.QrAdmin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;

import androidx.appcompat.app.AppCompatActivity;

public class activity_login extends AppCompatActivity {
//Declaramos nuestra variable de conexion
    private static clsConexionPG con=new clsConexionPG();
    private static clsConexionCP con2=new clsConexionCP();
    Variables_Globales va=Variables_Globales.getInstance();

    static String cadenaConexion = "jdbc:postgresql://gepesat-postgresql-do-user-7581876-0.a.db.ondigitalocean.com:25060/defaultdb?sslfactory=org.postgresql.ssl.NonValidatingFactory&ssl=true";
    static String respuestaSql= "vacia";

    //Declaramos los elementos de nuestro activity login
    Button btniniciar;
    EditText txtusuario,txtclave;
    Boolean Recupera=false;
    Connection conexxion=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //if ((!Recupera) && activeNetwork(getApplicationContext())) {
        SharedPreferences sharedPreferences = getSharedPreferences("myQR", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("myQRKey", "");
        editor.commit();
        if ((!Recupera) && isOnline() && isNetworkAvailable()) {
            Actualizar();
        }

        //Luego a esas variables le asignamos los valores de cada elemento
        btniniciar=findViewById(R.id.btniniciar);
        txtusuario=findViewById(R.id.txtusuario);
        txtclave=findViewById(R.id.txtclave);

        btniniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Inicio_Sesion(txtusuario.getText().toString(),txtclave.getText().toString());
            }
        });
    }
    public boolean activeNetwork (Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }

    public boolean VerificaRed () {
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

    public void Actualizar(){

        Statement sentencia = null;
        ResultSet resultado = null;
        try {
            Toast.makeText(getApplicationContext(), "ACTUALIZANDO", Toast.LENGTH_SHORT).show();
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Class.forName("org.postgresql.Driver");
            conexxion = DriverManager.getConnection(cadenaConexion,"doadmin","fmawbjj6kmdbc330");
            sentencia = conexxion.createStatement();
            String consultaSQL = "SELECT id_conductor, nom_conductor, email_coductor, clave_conductor FROM gpn_conductor WHERE id_user='404' ";
            resultado = sentencia.executeQuery(consultaSQL);
            respuestaSql = "";
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
                Recupera = true;
                Toast.makeText(getApplicationContext(), "ACTUALIZACION FINALIZADA", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "USUARIO O CLAVE INCORRECTO", Toast.LENGTH_SHORT).show();
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
    }

    //Crearemos la Función para Iniciar Sesion de Postgresql
    public  void Inicio_Sesion(String usuario, String clave){
      Toast.makeText(getApplicationContext(), "VALIDANDO DATOS", Toast.LENGTH_SHORT).show();
      if (isOnline() && isNetworkAvailable()) {
          try {
              String storeProcedureCall = "{CALL pa_logueo_android(?,?,?,?,?)}";
              //CallableStatement cStmt=con.conexionBD().prepareCall(storeProcedureCall);
              CallableStatement cStmt = con.conexionBD().prepareCall(storeProcedureCall);
              //Estos dos primeros parametros son los de entrada
              cStmt.setString(1, usuario);
              cStmt.setString(2, clave);

              // y estos  6 ultimos son los de salida aqui solo le indicamos que tipo de parametro se
              cStmt.registerOutParameter(3, Types.INTEGER);
              cStmt.registerOutParameter(4, Types.VARCHAR);
              cStmt.registerOutParameter(5, Types.VARCHAR);

              cStmt.executeUpdate();

              //declaramos las variables que recibiremos de la funcion de postgresql

              Integer _codigo = cStmt.getInt(3);
              String _datos = cStmt.getString(4);
              String _msj = cStmt.getString(5);

              //agregamos una condicional para sercioranos si se ingresaron correctamente el usuario y la clave

              if (_msj.equals("OK")) {
                  //si es correcto cargamos el menu con los datos del usuario
                  va.set_codigousuario(_codigo);
                  va.set_datos(_datos);

                  //Luego Abrimos el activity menu
                  Intent menu = new Intent(this, activity_menu.class);
                  startActivity(menu);

              } else {
                  Toast.makeText(getApplicationContext(), _msj, Toast.LENGTH_SHORT).show();
              }

          } catch (Exception er) {
              Toast.makeText(getApplicationContext(), er.toString(), Toast.LENGTH_SHORT).show();
          }
      } else {
          SQLiteDatabase myDB = openOrCreateDatabase("my.db", MODE_PRIVATE, null);
          //myDB.execSQL(
          //        "CREATE TABLE IF NOT EXISTS gpn_conductor (id_conductor VARCHAR(50), email_coductor VARCHAR(50), clave_conductor VARCHAR(20))"
          //);
          Cursor myCursor = myDB.rawQuery("SELECT * FROM gpn_conductor WHERE email_coductor='" + usuario +"' and clave_conductor='"+clave+"'", null);

          String selected = "";

          if (myCursor != null && myCursor.moveToFirst()) {

              while (!myCursor.isAfterLast()) {

                  //get items at selected position
                  //selected = column.get(where);

                  Integer _codigo = myCursor.getInt(0);
                  String _datos = myCursor.getString(1);
                  String _msj = "OK";

                  //agregamos una condicional para sercioranos si se ingresaron correctamente el usuario y la clave

                  if (_msj.equals("OK")) {
                      //si es correcto cargamos el menu con los datos del usuario
                      va.set_codigousuario(_codigo);
                      va.set_datos(_datos);

                      //Luego Abrimos el activity menu
                      Intent menu = new Intent(this, activity_menu.class);
                      startActivity(menu);

                  } else {
                      Toast.makeText(getApplicationContext(), _msj, Toast.LENGTH_SHORT).show();
                  }
                  myCursor.moveToNext();
              }
              myCursor.close();
          } else {
              Toast.makeText(getApplicationContext(), "USUARIO O CLAVE INCORRECTO", Toast.LENGTH_SHORT).show();
          }

          myDB.close();
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
}
