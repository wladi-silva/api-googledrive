package com.wladi;

public class Maker {
    
    public static void authenticationMaker() {
        try {
            com.wladi.GoogleDrive.authentication();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String uploadMaker(String arquivo) {
        try {
            return com.wladi.GoogleDrive.upload(arquivo);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void downloadMaker(String id, String nome) {
        try {
            com.wladi.GoogleDrive.download(id, nome);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}