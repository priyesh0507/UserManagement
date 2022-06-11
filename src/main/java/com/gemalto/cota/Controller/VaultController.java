package com.gemalto.cota.Controller;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gemalto.cota.entities.UserRequest;
import com.gemalto.cota.repo.UserRequestRepository;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.Versioned;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class VaultController {

	final private static String baseUrl = "http://127.0.0.1:8200/v1/";

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	@Autowired
	private UserRequestRepository requestRepository;

	@Autowired
	private RestTemplate restTemplate;

	// Find user token
	@GetMapping("/loginUser/{token}")
	ResponseEntity<String> loginUser(
			@PathVariable
			String token)
			throws URISyntaxException, JsonProcessingException {
		boolean admin = false;
		if (token.contains("vault")) {
			//String actualToken=DecryptToken(clientToken, token);
			//	createToken(actualToken, org);
		}
		ResponseEntity<String> response;
		// "hvs.oM5b5tfyiR1ZUvl9qinS2u2g"
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("X-Vault-Token", token);
		String url = baseUrl.concat("auth/token/lookup");

		HttpEntity<String> request = new HttpEntity<String>(null, headers);

		ResponseEntity<String> restResponse = restTemplate.postForEntity(url,
				request, String.class);
		System.out.println(restResponse.getStatusCodeValue());
		if (restResponse.getStatusCodeValue() == 200) {
			JsonNode root = objectMapper.readTree(restResponse.getBody()).get(
					"data");
			JsonNode policies = root.get("policies");
			for (JsonNode jsonNode : policies) {
				if (jsonNode.asText().equals("root")) {
					admin = true;
				}
			}
			if (admin) {
				response = new ResponseEntity<>("admin", headers, HttpStatus.OK);
			} else {
				response = new ResponseEntity<>("user", headers, HttpStatus.OK);
			}
		} else {
			response = new ResponseEntity<>("unauthorized", headers,
					HttpStatus.FORBIDDEN);

		}
		return response;
	}

	// Save
	@PostMapping("/usersRequest")
	// return 201 instead of 200
	@ResponseStatus(HttpStatus.CREATED)
	UserRequest newUser(
			@RequestBody
			UserRequest newUserRequest) {
		return requestRepository.save(newUserRequest);
	}

	// get in progress userRequest
	@GetMapping("/userRequest/{requestStatus}")
	List<UserRequest> findAllUserRequest(
			@PathVariable
			String requestStatus) {
		System.out.println(requestStatus);
		return requestRepository.findAllByRequestStatus(requestStatus);
	}

	@GetMapping("/orgRequest/{organisation}")
	List<UserRequest> RequestOrg(
			@PathVariable
			String organisation) {
		System.out.println(organisation);
		return requestRepository.findAllByOrganization(organisation);
	}

	// Find user token
	@GetMapping("/grant/{token}/{policy}")
	ResponseEntity<String> grantAccess(
			@PathVariable
			String token,
			@PathVariable
			String policy) throws URISyntaxException,
			JsonProcessingException {
		boolean admin = false;
		ResponseEntity<String> response;
		// "hvs.oM5b5tfyiR1ZUvl9qinS2u2g"
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("X-Vault-Token", token);
		String url = baseUrl.concat("secret/data/{path}");

		HttpEntity<String> request = new HttpEntity<String>(null, headers);

		try {
			ResponseEntity<String> restResponse = restTemplate.exchange(url,
					HttpMethod.GET, request, String.class, policy);

			/*
			 * if (restResponse.getStatusCodeValue() == 200) {
			 * System.out.println("secret exists");
			 *
			 * } else{
			 *
			 * }
			 */
		} catch (Exception e) {
			System.out.println("create secret");

			String createSecretUrl = baseUrl.concat("secret/data/{path}");

			JSONObject main = new JSONObject();
			JSONObject user = new JSONObject();
			user.put("Organization", policy);
			main.put("data", user);
			// object.put("data", main);
			request = new HttpEntity<String>(main.toString(), headers);

			ResponseEntity<String> restResponse =
					restTemplate.postForEntity(createSecretUrl, request,
							String.class, policy);

			System.out.println(restResponse.getStatusCodeValue());

			createPolicy(token, policy);

			createToken(token, policy);
		}

		/*
		 * System.out.println(restResponse.getStatusCodeValue()); if
		 * (restResponse.getStatusCodeValue() == 200) { JsonNode root =
		 * objectMapper.readTree(restResponse.getBody()).get("data");
		 * System.out.println(root); JsonNode policies = root.get("policies");
		 * for (JsonNode jsonNode : policies) { if
		 * (jsonNode.asText().equals("root")) { admin = true; } } if (admin) {
		 * response = new ResponseEntity<>("admin", headers, HttpStatus.OK); }
		 * else { response = new ResponseEntity<>("user", headers,
		 * HttpStatus.OK); } } else { response = new
		 * ResponseEntity<>("unauthorized", headers, HttpStatus.FORBIDDEN);
		 *
		 * }
		 */
		return null;
	}

	// creation of policy
	public String createPolicy(String token, String policyName) {

		System.out.println("creating policy");

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("X-Vault-Token", token);

		String createSecretUrl = baseUrl.concat("sys/policies/acl/{policy}");

		JSONObject policy = new JSONObject();
		/*
		 * JSONObject path = new JSONObject(); JSONObject tenant = new
		 * JSONObject(); JSONObject capabilities = new JSONObject();
		 *
		 * List<String> list = new ArrayList<String>(); list.add("create");
		 * list.add("read"); list.add("update"); list.add("list");
		 * list.add("delete"); list.add("sudo");
		 *
		 * JSONArray array = new JSONArray(); for(int i = 0; i < list.size();
		 * i++) { array.put(list.get(i));
		 *
		 * }
		 */
		String str = "path \"secret/data/tenant\"{ capabilities = [\"create\", \"read\", \"update\", \"delete\", \"list\", \"sudo\"]}";

		String updated_policy = str.replaceAll("tenant", policyName);
		String policy_name = policyName + "_policy";

		/*
		 * JSONObject childCapabilities = new JSONObject();
		 * childCapabilities.put("capabilities", array);
		 *
		 * List<JSONObject> tenantArray = new ArrayList<JSONObject>();
		 *
		 * tenantArray.add(childCapabilities);
		 *
		 * // List<String> tennantList = new ArrayList<String>();
		 * //tennantList.add("secret/data/gemalto");
		 *
		 * tenant.put("secret/data/gemalto",tenantArray);
		 *
		 * List<JSONObject> pathArray = new ArrayList<JSONObject>();
		 *
		 * pathArray.add(tenant);
		 *
		 * path.put("path",pathArray);
		 *
		 * // tenant.put("secret//data//gemalto",childCapabilities); //
		 * path.put("path",tenant);
		 */
		policy.put("policy", updated_policy);

		// System.out.println(path.toString());

		HttpEntity<String> request = new HttpEntity<String>(policy.toString(),
				headers);

		ResponseEntity<String> restResponse = restTemplate.postForEntity(
				createSecretUrl, request, String.class, policy_name);

		System.out.println(restResponse.getStatusCodeValue());

		return null;
	}

	// creation of token
	public String createToken(String token, String policyName) {

		System.out.println("creating token");

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("X-Vault-Token", token);

		String createSecretUrl = baseUrl.concat("auth/token/create");

		JSONObject policy = new JSONObject();

		List<String> list = new ArrayList<String>();

		list.add("generic_policy");
		list.add(policyName + "_policy");

		JSONArray array = new JSONArray();
		for (int i = 0; i < list.size(); i++) {
			array.put(list.get(i));

		}

		JSONObject policiesObj = new JSONObject();
		policiesObj.put("policies", array);
		policiesObj.put("no_default_policy", true);

		HttpEntity<String> request = new HttpEntity<String>(
				policiesObj.toString(), headers);

		ResponseEntity<String> restResponse = restTemplate.postForEntity(
				createSecretUrl, request, String.class, policyName);

		System.out.println(restResponse.getStatusCodeValue());

		try {
			JsonNode root = objectMapper.readTree(restResponse.getBody()).get(
					"auth");
			JsonNode root1 = root.get("client_token");
			String client_token = root1.asText();

			System.out.println(root);
			System.out.println(client_token);

			String dummyToken = createTokenForEncryption(token, "encrypt");

			String encrypted_key = EncryptToken(dummyToken, client_token);
			String decrypted_key = DecryptToken(dummyToken, encrypted_key);
			System.out.println("Dec Key ::" + decrypted_key);
			return client_token;

		} catch (Exception e) {

		}

		return null;
	}

	public String EncryptToken(String dummyToken, String token) {
		String ciphered_token = "";
		try {

			System.out.println("encrypting data");

			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add("X-Vault-Token", dummyToken);

			String createSecretUrl = baseUrl.concat("transit/encrypt/{name}");

			JSONObject policy = new JSONObject();

			// Encode data on your side using BASE64
			byte[] bytesEncoded = Base64.encodeBase64(token.getBytes());
			String tokenBase64 = new String(bytesEncoded);
			System.out.println("encoded value is " + new String(bytesEncoded));

			/*
			 * // Decode data on other side, by processing encoded data byte[]
			 * valueDecoded = Base64.decodeBase64(bytesEncoded);
			 * System.out.println("Decoded value is " + new
			 * String(valueDecoded));
			 */

			policy.put("plaintext", tokenBase64);
			HttpEntity<String> request = new HttpEntity<String>(
					policy.toString(), headers);

			ResponseEntity<String> restResponse = restTemplate.postForEntity(
					createSecretUrl, request, String.class, "user_new");

			JsonNode root = objectMapper.readTree(restResponse.getBody()).get(
					"data");
			System.out.println(root);
			JsonNode root1 = root.get("ciphertext");
			ciphered_token = root1.asText();
			System.out.println(ciphered_token);

		} catch (Exception e) {

		}
		return ciphered_token;

	}

	public String DecryptToken(String token, String encryptedData) {
		String tokenBase64Dec = "";
		try {

			System.out.println("decrypting data");

			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add("X-Vault-Token", token);

			String createSecretUrl = baseUrl.concat("transit/decrypt/{name}");

			JSONObject policy = new JSONObject();

			policy.put("ciphertext", encryptedData);

			HttpEntity<String> request = new HttpEntity<String>(
					policy.toString(), headers);

			ResponseEntity<String> restResponse = restTemplate.postForEntity(
					createSecretUrl, request, String.class, "user_new");

			JsonNode root = objectMapper.readTree(restResponse.getBody()).get(
					"data");
			System.out.println(root);
			JsonNode root1 = root.get("plaintext");
			String plain_text = root1.asText();
			System.out.println(plain_text);

			// Decode data on your side using BASE64
			byte[] bytesDecoded = Base64.decodeBase64(plain_text.getBytes());
			tokenBase64Dec = new String(bytesDecoded);
			System.out
					.println("Decoded value is " + new String(tokenBase64Dec));

			/*
			 * // Decode data on other side, by processing encoded data byte[]
			 * valueDecoded = Base64.decodeBase64(bytesEncoded);
			 * System.out.println("Decoded value is " + new
			 * String(valueDecoded));
			 */
			System.out.println(tokenBase64Dec);

		} catch (Exception e) {

		}
		return tokenBase64Dec;

	}

	// creation of token
	public String createTokenForEncryption(String token, String policyName) {

		System.out.println("creating token for encryption");
		String client_token = "";
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("X-Vault-Token", token);

		String createSecretUrl = baseUrl.concat("auth/token/create");

		JSONObject policy = new JSONObject();

		List<String> list = new ArrayList<String>();

		list.add(policyName + "_policy");

		JSONArray array = new JSONArray();
		for (int i = 0; i < list.size(); i++) {
			array.put(list.get(i));

		}

		JSONObject policiesObj = new JSONObject();
		policiesObj.put("policies", array);
		policiesObj.put("no_default_policy", true);

		HttpEntity<String> request = new HttpEntity<String>(
				policiesObj.toString(), headers);

		ResponseEntity<String> restResponse = restTemplate.postForEntity(
				createSecretUrl, request, String.class, policyName);

		System.out.println(restResponse.getStatusCodeValue());

		try {
			JsonNode root = objectMapper.readTree(restResponse.getBody()).get(
					"auth");
			JsonNode root1 = root.get("client_token");
			client_token = root1.asText();

			System.out.println(root);
			System.out.println(client_token);

		} catch (Exception e) {

		}

		return client_token;
	}

	@PostMapping("/putData/{token}/{policy}/{key}/{value}")
	public void putDataOnPath(
			@PathVariable
			String token,
			@PathVariable
			String policy,
			@PathVariable
			String key,
			@PathVariable
			String value) {

		VaultEndpoint vaultEndpoint = new VaultEndpoint();

		vaultEndpoint.setHost("127.0.0.1");
		vaultEndpoint.setPort(8200);
		vaultEndpoint.setScheme("http");

		// Authenticate
		VaultTemplate vaultTemplate = new VaultTemplate(vaultEndpoint,
				new TokenAuthentication(token));

		// Write a secret
		Map<String, String> data = new HashMap<>();
		data.put(key, value);

		vaultTemplate.opsForVersionedKeyValue("secret").put(policy, data);

		System.out.println("Secret written successfully.");

	}

	@GetMapping("/getData/{token}/{policy}/{key}/{value}")
	public String getDataOnPath(
			@PathVariable
			String token,
			@PathVariable
			String policy,
			@PathVariable
			String key,
			@PathVariable
			String value) {
		VaultEndpoint vaultEndpoint = new VaultEndpoint();

		vaultEndpoint.setHost("127.0.0.1");
		vaultEndpoint.setPort(8200);
		vaultEndpoint.setScheme("http");

		// Authenticate
		VaultTemplate vaultTemplate = new VaultTemplate(vaultEndpoint,
				new TokenAuthentication(token));

		/*
		 * System.out.println("Here we gooooo"); VaultResponse response =
		 * vaultTemplate.read("secret/thales");
		 *
		 * String rp=response.toString();
		 *
		 * System.out.println(rp);
		 */

		Versioned<Map<String, Object>> secret = vaultTemplate
				.opsForVersionedKeyValue("secret").get(policy);

		String passwordFromVault = "";

		if (secret != null && secret.hasData()) {
			passwordFromVault = (String) secret.getData().get(key);

			System.out.println(passwordFromVault);

		}
		return passwordFromVault;
	}

}
