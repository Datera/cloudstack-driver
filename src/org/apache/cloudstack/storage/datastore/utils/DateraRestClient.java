package org.apache.cloudstack.storage.datastore.utils;

import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class DateraRestClient {

    private Gson gson = new GsonBuilder().create();
    private LoginResponse respLogin = new LoginResponse();
    private String managementIp;
 private int managementPort;
 private String userName;
 private String password;
 private final String defaultStorageName = "storage-1";
 private final String defaultVolumeName = "volume-1";

 public DateraRestClient(String ip, int port,String user, String pass)
 {
  managementIp = ip;
  managementPort = port;
  userName = user;
  password = pass;
  doLogin();
 }


 private class InitiatorGroup
 {
  public String name;
  public List<String> members = null;

  public InitiatorGroup(String paramName, List<String> paramMembers)
  {
   this.name = paramName;
   this.members = paramMembers;
  }
 }

 private class LoginModel
 {
  public String name;
  public String password;
 }

 public class ACLPolicyModel
 {
  public List<String> initiators; // e.g. "/initiators/iqn.1993-08.org.debian:01:6e3ca7bd74e1"
  @SerializedName("initiator_groups")
  public List<String> initiatorGroups; // e.g. "/initiator_groups/cluster2_initiator_group"

  public ACLPolicyModel(List<String> paramInitiators, List<String> paramInitiatorGroups)
  {
   initiators = paramInitiators;
   initiatorGroups = paramInitiatorGroups;
  }
 }
 public class AppModel
 {
  public String name;
  @SerializedName("access_control_mode")
  public String accessControlMode;
  @SerializedName("storage_instances")
  public StorageInstanceModel storageInstances;

  public AppModel(String appInstanceName, String accessControlMode, StorageInstanceModel inst)
  {
   name = appInstanceName;
   accessControlMode = accessControlMode;
   storageInstances = inst;
  }
 }

 public class StorageInstanceModel
 {
  @SerializedName(defaultStorageName)
  public StorageModel storage1;

  public StorageInstanceModel(StorageModel st)
  {
   storage1 = st;
  }
 }

 public class StorageModel
 {
  public String name = defaultStorageName;
  public VolumeInstanceModel volumes;
  @SerializedName("acl_policy")
  public ACLPolicyModel aclPolicy;
  public StorageModel(VolumeInstanceModel vols, ACLPolicyModel policy)
  {
   volumes = vols;
   aclPolicy = policy;
  }
 }

 public class VolumeInstanceModel
 {
  @SerializedName(defaultVolumeName)
  public VolumeModel volume1;

  public VolumeInstanceModel(VolumeModel vol)
  {
   volume1 = vol;
  }
 }
 public class VolumeModel {

  public String name = defaultVolumeName;
  public int size;
  @SerializedName("replica_count")
  public int replicaCount;
  //snapshot policies

  public VolumeModel(int volSize, int volReplicaSize)
  {
   size = volSize;
   replicaCount = volReplicaSize;
  }
 }

 public class StorageResponse
 {
  public class AccessControl
  {
   public List<String> ips;
   public String iqn;
  }

  public AccessControl access;

 }

 public StorageResponse getStorageInfo(String appInstance, String storageInstance)
 {
  String restPath = String.format("/v2/app_instances/%s/storage_instances/%s", appInstance,storageInstance);
  HttpGet getRequest = new HttpGet(restPath);
        getRequest.setHeader("auth-token",respLogin.getKey());
        String response = execute(getRequest);

        StorageResponse storageInfo = gson.fromJson(response, StorageResponse.class);

        return storageInfo;
 }

 public void getInitiatorGroup()
 {

 }

 public void createInitiatorGroup(String name, List<String> initiators)
 {
  HttpPost postRequest = new HttpPost("/v2/initiator_groups");
  postRequest.setHeader("Content-Type","application/json");
  postRequest.setHeader("auth-token",respLogin.getKey());

  InitiatorGroup intrGroup = new InitiatorGroup(name,initiators);
  String payload = gson.toJson(intrGroup);

  try {
   StringEntity params = new StringEntity(payload);
        postRequest.setEntity(params);
  } catch (UnsupportedEncodingException e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
  }
  execute(postRequest);
 }

 public void getInitiators()
 {
       HttpGet getRequest = new HttpGet("/v2/initiators");
       getRequest.setHeader("auth-token",respLogin.getKey());
       execute(getRequest);
 }
 public void createVolume(String appInstanceName, List<String> initiators,int volumeGB, String accessControlMode, List<String> initiatorGroups)
 {
       HttpPost postRequest = new HttpPost("/v2/app_instances");
       postRequest.setHeader("Content-type","application/json");
       postRequest.setHeader("auth-token",respLogin.getKey());

       String payload = generateVolumePayload(appInstanceName,initiators,volumeGB,accessControlMode,initiatorGroups);

  try {
   StringEntity params = new StringEntity(payload);
        postRequest.setEntity(params);
  } catch (UnsupportedEncodingException e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
  }

       execute(postRequest);

 }

 public String generateVolumePayload(String appInstanceName,
   List<String> initiators, int volumeGB, String accessControlMode, List<String> initiatorGroups) {
  // TODO Auto-generated method stub
  String payload = "";
  VolumeModel vol = new VolumeModel(volumeGB,3);

  AppModel app = new AppModel(appInstanceName,accessControlMode,
    new StorageInstanceModel(
      new StorageModel(
        new VolumeInstanceModel(
          new VolumeModel(volumeGB, 3)),new ACLPolicyModel(initiators,initiatorGroups))));

  payload = gson.toJson(app);

  System.out.println(payload);
  return payload;
 }
 private void doLogin()
 {
     HttpPut postRequest = new HttpPut("/v2/login");
     postRequest.setHeader("Content-Type","application/json");
     LoginModel loginInfo = new LoginModel();
     loginInfo.name = userName;
     loginInfo.password = password;

  try {
   StringEntity params = new StringEntity(gson.toJson(loginInfo));
        postRequest.setEntity(params);
  } catch (UnsupportedEncodingException e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
  }

  String resp = execute(postRequest);
  if(null != resp)
  {
   respLogin = gson.fromJson(resp, LoginResponse.class);
  }

  System.out.println("The session key :"+respLogin.getKey());

 }
 private String execute(HttpRequest request)
 {
  DefaultHttpClient httpclient = new DefaultHttpClient();
  String resp = "";
 try {

  httpclient = getHttpClient(managementPort);

        AuthScope authScope = new AuthScope(managementIp, managementPort, AuthScope.ANY_SCHEME);
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userName, password);

        httpclient.getCredentialsProvider().setCredentials(authScope, credentials);

 // specify the host, protocol, and port
 HttpHost target = new HttpHost(managementIp, managementPort, "https");

      // specify the get request
      System.out.println("executing request to " + target);

      HttpResponse httpResponse = httpclient.execute(target, request);
      HttpEntity entity = httpResponse.getEntity();

      System.out.println("----------------------------------------");
      System.out.println(httpResponse.getStatusLine());
      Header[] headers = httpResponse.getAllHeaders();
      for (int i = 0; i < headers.length; i++) {
        System.out.println(headers[i]);
      }
      System.out.println("----------------------------------------");

      resp = EntityUtils.toString(entity);

      if (entity != null) {
        System.out.println(resp);
      }

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      // When HttpClient instance is no longer needed,
      // shut down the connection manager to ensure
      // immediate deallocation of all system resources
      httpclient.getConnectionManager().shutdown();
    }

 return resp;
 }

    private DefaultHttpClient getHttpClient(int iPort) {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1"); //SSLUtils.getSSLContext();
            X509TrustManager tm = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[] {tm}, new SecureRandom());

            SSLSocketFactory socketFactory = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            SchemeRegistry registry = new SchemeRegistry();

            registry.register(new Scheme("https", iPort, socketFactory));

            BasicClientConnectionManager mgr = new BasicClientConnectionManager(registry);
            DefaultHttpClient client = new DefaultHttpClient();

            return new DefaultHttpClient(mgr, client.getParams());
        } catch (NoSuchAlgorithmException ex) {
            //throw new CloudRuntimeException(ex.getMessage());
        } catch (KeyManagementException ex) {
            //throw new CloudRuntimeException(ex.getMessage());
        }
        return null;
    }

}
