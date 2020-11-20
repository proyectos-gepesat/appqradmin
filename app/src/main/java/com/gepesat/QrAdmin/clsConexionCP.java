package com.gepesat.QrAdmin;

import android.os.StrictMode;

import java.sql.Connection;
import java.sql.DriverManager;

public class clsConexionCP {
    Connection conexion=null;

    //Creamos nuestra funcion para Conectarnos a Postgresql
    public  Connection conexionBD2(){
        try{
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Class.forName("org.postgresql.Driver");
            conexion = DriverManager.getConnection("jdbc:postgresql://gepesat-postgresql-do-user-7581876-0.a.db.ondigitalocean.com:25060/captura?sslfactory=org.postgresql.ssl.NonValidatingFactory&ssl=true", "doadmin", "fmawbjj6kmdbc330");
            //conexion = DriverManager.getConnection("jdbc:postgresql://192.168.9.13:5432/dbcrud_postgresql", "postgres", "admin");
        }catch (Exception er){
            System.err.println("Error Conexion"+ er.toString());
        }
        return  conexion;
    }

    //Creamos la funcion para Cerrar la Conexion
    protected  void cerrar_conexion(Connection con)throws  Exception{
        con.close();
    }
}
