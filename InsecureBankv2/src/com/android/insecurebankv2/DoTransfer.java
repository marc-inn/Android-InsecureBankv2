package com.android.insecurebankv2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/*
The page that allows the user to transfer an amount between two accounts
@author Dinesh Shetty
*/
public class DoTransfer extends Activity {

	String result, result2, trf_username = null, trf_password = null;
	String passNormalized;
	String usernameBase64ByteString;
	BufferedReader reader;
	//	The EditText that holds the from account number
	EditText from, trf_from;
	//	The EditText that holds the to account number
	EditText to, trf_to;
	//	The EditText that holds the to amount to be transferred between the accounts
	EditText amount;
	/*The EditText that takes the Phone number as input from the user. A confirmation of 
	successful transfer is sent to this phone number*/
	EditText phoneNumber;
	String number = "1234";
	TextView fr;
	//	The Button that handles the from and to account autofill operation on the basis of logged in user
	Button getAccounts;
	//	The Button that handles the transfer operation activity
	Button transfer;
	String acc1, acc2;
	HttpResponse responseBody;
	String passBase64String;
	JSONObject jsonObject;
	InputStream in ;
	String serverip = "";
	String serverport = "";
	String protocol = "http://";
	Button button1;
	TextView tv1;
	EditText ed1;
	BufferedWriter wr = null;
	public static final String MYPREFS2 = "mySharedPreferences";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_do_transfer);
		SharedPreferences serverDetails;
		serverDetails = PreferenceManager.getDefaultSharedPreferences(this);
		serverip = serverDetails.getString("serverip", null);
		serverport = serverDetails.getString("serverport", null);
		transfer = (Button) findViewById(R.id.button_Transfer);
		transfer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				from = (EditText) findViewById(R.id.editText_from);
				to = (EditText) findViewById(R.id.editText_to);
				new RequestDoTransferTask().execute("username");
			}
		});

		button1 = (Button) findViewById(R.id.button_CreateUser);
		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new RequestDoGets2().execute("username");
			}
		});
	}

	public class RequestDoTransferTask extends AsyncTask < String, String, String > {

		/**
		 * constructor
		 * @return 
		 */
		public void AsyncHttpTransferPost(String string) {
			//do something
		}

		/**
		 * background
		 */
		@Override
		protected String doInBackground(String...params) {
			String str = "";
			str = "dinesh";
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(protocol + serverip + ":" + serverport + "/dotransfer");
			SharedPreferences settings = getSharedPreferences(MYPREFS2, 0);
			final String username = settings.getString("EncryptedUsername", null);
			byte[] usernameBase64Byte = Base64.decode(username, Base64.DEFAULT);
			try {
				usernameBase64ByteString = new String(usernameBase64Byte, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			final String password = settings.getString("superSecurePassword", null);
			try {
				//	Stores the decrypted form of the password from the locally stored shared preference file
				passNormalized = getNormalizedPassword(password);
			} catch (InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			List < NameValuePair > nameValuePairs = new ArrayList < NameValuePair > (5);
			nameValuePairs.add(new BasicNameValuePair("username", usernameBase64ByteString));
			nameValuePairs.add(new BasicNameValuePair("password", passNormalized));
			from = (EditText) findViewById(R.id.editText_from);
			to = (EditText) findViewById(R.id.editText_to);
			amount = (EditText) findViewById(R.id.editText_amount);
			nameValuePairs.add(new BasicNameValuePair("from_acc", from.getText().toString()));
			nameValuePairs.add(new BasicNameValuePair("to_acc", to.getText().toString()));
			nameValuePairs.add(new BasicNameValuePair("amount", amount.getText().toString()));
			try {
				//	The HTTP Post of the credentials plus the transaction information
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				//	Stores the HTTP response of the transaction activity
				responseBody = httpclient.execute(httppost);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try { in = responseBody.getEntity().getContent();
			} catch (IllegalStateException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				result = convertStreamToString( in );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			result = result.replace("\n", "");
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					AsyncHttpTransferPost("result");
					if (result != null) {
						if (result.indexOf("Success") != -1) {
							Toast.makeText(getApplicationContext(), "Transfer Successful", Toast.LENGTH_LONG).show();
							//	Function that sends a confirmation SMS if transaction was successful
							broadcastSMS();

							try {
								jsonObject = new JSONObject(result);
								acc1 = jsonObject.getString("from");
								acc2 = jsonObject.getString("to");
								System.out.println("Message:" + jsonObject.getString("message") + " From:" + from.getText().toString() + " To:" + to.getText().toString() + " Amount:" + amount.getText().toString());
								final String status = new String("\nMessage:" + "Success" + " From:" + from.getText().toString() + " To:" + to.getText().toString() + " Amount:" + amount.getText().toString() + "\n");
								try {
									//	Captures the successful transaction status for Transaction history tracking
									String MYFILE = Environment.getExternalStorageDirectory() + "/Statements_" + usernameBase64ByteString + ".html";
									BufferedWriter out2 = new BufferedWriter(new FileWriter(MYFILE, true));
									out2.write(status);
									out2.close();
								} catch (IOException e) {
									e.toString();
								}
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
							Toast.makeText(getApplicationContext(), "Transfer Failed", Toast.LENGTH_LONG).show();
							System.out.println("Message:" + "Failure" + " From:" + from.getText().toString() + " To:" + to.getText().toString() + " Amount:" + amount.getText().toString());
							final String status = new String("\nMessage:" + "Failure" + " From:" + from.getText().toString() + " To:" + to.getText().toString() + " Amount:" + amount.getText().toString() + "\n");
							//   Captures the failed transaction status for Transaction history tracking
							String MYFILE = Environment.getExternalStorageDirectory() + "/Statements_" + usernameBase64ByteString + ".html";
							try {
								BufferedWriter out2 = new BufferedWriter(new FileWriter(MYFILE, true));
								out2.write(status);
								out2.close();
							} catch (IOException e) {
								e.toString();
							}
						}
					}
				}

				/*
				The function that handles the SMS activity
				phoneNumber: Phone number to which the confirmation SMS is to be sent
				*/
				private void broadcastSMS() {
					phoneNumber = (EditText) findViewById(R.id.editText_Phone);
					number = phoneNumber.getText().toString().trim();

					if (number != "") {
						// TODO Auto-generated method stub
						Intent smsIntent = new Intent();
						smsIntent.setAction("theBroadcast");
						smsIntent.putExtra("phonenumber", number);
						sendBroadcast(smsIntent);
					}
				}
			});
			return str;
		}

		@Override
		protected void onPostExecute(String result) {}

		protected void onProgressUpdate(String...progress) {}
	}

	public class RequestDoGets2 extends AsyncTask < String, String, String > {
		/**
		 * constructor
		 * @return 
		 */
		public void AsyncHttpPost(String string) {
			//do something
		}

		/**
		 * background
		 */
		@Override
		public String doInBackground(String...params) {
			String str = "";
			str = "dinesh";
			HttpClient httpclient = new DefaultHttpClient();

			HttpPost httppost = new HttpPost(protocol + serverip + ":" + serverport + "/getaccounts");
			SharedPreferences settings = getSharedPreferences(MYPREFS2, 0);
			final String username = settings.getString("EncryptedUsername", null);
			byte[] usernameBase64Byte = Base64.decode(username, Base64.DEFAULT);

			try {
				usernameBase64ByteString = new String(usernameBase64Byte, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			final String password = settings.getString("superSecurePassword", null);
			try {
				//	Stores the decrypted form of the password from the locally stored shared preference file
				passNormalized = getNormalizedPassword(password);
			} catch (InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// Add your data
			List < NameValuePair > nameValuePairs = new ArrayList < NameValuePair > (2);
			nameValuePairs.add(new BasicNameValuePair("username", usernameBase64ByteString));
			nameValuePairs.add(new BasicNameValuePair("password", passNormalized));
			try {
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				//	Stores the HTTP response of the get account numbers activity
				responseBody = httpclient.execute(httppost);
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

			try { in = responseBody.getEntity().getContent();
			} catch (IllegalStateException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				result = convertStreamToString( in );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			result = result.replace("\n", "");
			//	Parsing of the result of HTTP request
			if (result != null) {
				if (result.indexOf("Correct") != -1) {
					try {
						jsonObject = new JSONObject(result);
						acc1 = jsonObject.getString("from");
						acc2 = jsonObject.getString("to");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					AsyncHttpPost("result");
					from = (EditText) findViewById(R.id.editText_from);
					to = (EditText) findViewById(R.id.editText_to);
					from.setText(acc1);
					to.setText(acc2);
				}
			});
			return str;
		}

		/**
		 * on getting result
		 */
		@Override
		public void onPostExecute(String result) {}

		public void onProgressUpdate(String...progress) {}
	}

	/*
	The function that handles the aes256 decryption of the password from the encrypted password.
	password: Encrypted password input to the aes function
	returns: Plaintext password outputted by the aes function
	*/
	private String getNormalizedPassword(String password) throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		CryptoClass crypt = new CryptoClass();
		return crypt.aesDeccryptedString(password);
	}


	public String convertStreamToString(InputStream in ) throws IOException {
		// TODO Auto-generated method stub
		try {
			reader = new BufferedReader(new InputStreamReader( in , "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line + "\n");
		} in .close();
		return sb.toString();
	}
	// Added for menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// Added for menu
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			callPreferences();
			return true;
		} else if (id == R.id.action_exit) {
			Intent i = new Intent(getBaseContext(), LoginActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void callPreferences() {
		// TODO Auto-generated method stub
		Intent i = new Intent(this, FilePrefActivity.class);
		startActivity(i);
	}
}