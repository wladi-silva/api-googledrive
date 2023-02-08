package com.wladi;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class GoogleDrive {

  private static final String APPLICATION_NAME = "ServicoTeste";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final String TOKENS_DIRECTORY_PATH = "tokens";
  private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_READONLY);
  private static final String CREDENTIALS_FILE_PATH = "credentials.json";

  private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {

    InputStream in = GoogleDrive.class.getResourceAsStream(CREDENTIALS_FILE_PATH);

    if (in == null) {
      throw new FileNotFoundException("Arquivo não encontrado: " + CREDENTIALS_FILE_PATH);
    }

    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
        .setAccessType("offline")
        .build();

    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8080).build();
    Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    
    return credential;
  }

  public static void main(String... args) throws IOException, GeneralSecurityException {

    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
        .setApplicationName(APPLICATION_NAME)
        .build();
    
    FileOutputStream fileOutputStream;
    FileList result = service.files().list()
        .setPageSize(2)
        .setFields("nextPageToken, files(id, name)")
        .execute();

    List<File> files = result.getFiles();
    if (files == null || files.isEmpty()) {
      System.out.println("Não existem dados a serem exibidos.");
    } else {
      System.out.println("Arquivos:");
      for (File file : files) {
        try {
          fileOutputStream = new FileOutputStream(file.getName());
          service.files().get(file.getId()).executeMediaAndDownloadTo(fileOutputStream);
        } catch (Exception e) {
           System.out.println(e);
        }
         
      }
    }
  }

}