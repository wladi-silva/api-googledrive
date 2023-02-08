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
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class GoogleDrive {

  public static final String APPLICATION_NAME = "ServicoTeste";
  public static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  public static final String TOKENS_DIRECTORY_PATH = "tokens";
  public static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
  public static final String CREDENTIALS_FILE_PATH = "authentication/credentials.json";
  public static Drive service;
  public static FileOutputStream fileOutputStream;


  public static void authentication() throws GeneralSecurityException, IOException {
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT)).setApplicationName(APPLICATION_NAME).build();
  }

  public static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
    InputStream in = GoogleDrive.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
    if (in.equals(null)) {throw new FileNotFoundException("Arquivo não encontrado: " + CREDENTIALS_FILE_PATH);}

    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
      .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
      .setAccessType("offline")
      .build();

    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8080).build();
    Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    
    return credential;
  }

  public static void download(List<File> files) throws IOException {
    for (File file : files) {
      try {
        fileOutputStream = new FileOutputStream(file.getName());
        service.files().get(file.getId()).executeMediaAndDownloadTo(fileOutputStream);
        System.out.println("Download realizado.");
      } catch (Exception e) { System.out.println("Erro: " + e); }
    }    
  }

  public static void upload(String arquivoNome) throws IOException {
    File fileMetadata = new File();
    fileMetadata.setName(arquivoNome);

    java.io.File filePath = new java.io.File(arquivoNome);
    FileContent mediaContent = new FileContent(null, filePath);
    try {
      File file = service.files().create(fileMetadata, mediaContent).execute();
      System.out.println(file.getName());
      System.out.println("Upload realizado.");
    } catch (Exception e) { System.out.println("Erro: " + e); }
  }

  public static List<File> getList(Integer tamanho) throws IOException {
    
    FileList result = service.files().list().setPageSize(tamanho).execute();
    List<File> files = result.getFiles();
    
    if(files.isEmpty()){System.out.println("Lista está vazia.");} else{System.out.println("Arquivos encontrados.");}
    
    return files;
  }

  public static void main(String... args) throws IOException, GeneralSecurityException {

    authentication();
    upload("alterações.docx");
    download(getList(4));
  
  }

}