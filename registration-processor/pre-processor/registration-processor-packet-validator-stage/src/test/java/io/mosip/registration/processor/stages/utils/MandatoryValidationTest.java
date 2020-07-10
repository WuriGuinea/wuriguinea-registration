package io.mosip.registration.processor.stages.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import io.mosip.kernel.packetmanager.exception.ApiNotAccessibleException;
import io.mosip.kernel.packetmanager.spi.PacketReaderService;
import io.mosip.registration.processor.core.packet.dto.packetvalidator.PacketValidationDto;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.registration.processor.core.constant.MappingJsonConstants;
import io.mosip.registration.processor.core.util.JsonUtil;
import io.mosip.registration.processor.packet.storage.utils.Utilities;

import io.mosip.registration.processor.status.dto.InternalRegistrationStatusDto;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Utilities.class, JsonUtil.class, IOUtils.class })
public class MandatoryValidationTest {

	/** The adapter. */
	@Mock
	private PacketReaderService adapter;

	@Mock
	private Utilities utility;

	@Mock
	private InputStream inputStream;

	private static final String source = "default";

	/** The registration status dto. */
	private InternalRegistrationStatusDto registrationStatusDto;

	private PacketValidationDto packetValidationDto = new PacketValidationDto();

	@Mock
	ObjectMapper mapIdentityJsonStringToObject;

	@InjectMocks
	private MandatoryValidation mandatoryValidation = new MandatoryValidation();
	private String idJsonString;

	private String mappingJsonString;

	@Before
	public void setUp() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		File identityMappingjson = new File(classLoader.getResource("RegistrationProcessorIdentity.json").getFile());
		InputStream identityMappingjsonStream = new FileInputStream(identityMappingjson);
		try {
			mappingJsonString = IOUtils.toString(identityMappingjsonStream, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		idJsonString = "{\"identity\":{\"fullName\":[{\"language\":\"eng\",\"value\":\"Ragavendran V\"},{\"language\":\"ara\",\"value\":\"قشلشرثىيقشى ر\"}],\"dateOfBirth\":\"1999/01/01\",\"age\":20,\"gender\":[{\"language\":\"eng\",\"value\":\"Male\"},{\"language\":\"ara\",\"value\":\"الذكر\"}],\"residenceStatus\":[{\"language\":\"eng\",\"value\":\"Non-Foreigner\"},{\"language\":\"ara\",\"value\":\"غير أجنبي\"}],\"addressLine1\":[{\"language\":\"eng\",\"value\":\"Kumar street\"},{\"language\":\"ara\",\"value\":\"نعةشق سفقثثف\"}],\"addressLine3\":[{\"language\":\"eng\",\"value\":\"Line 3\"},{\"language\":\"ara\",\"value\":\"مىث ٣\"}],\"region\":[{\"language\":\"eng\",\"value\":\"Rabat Sale Kenitra\"},{\"language\":\"ara\",\"value\":\"جهة الرباط سلا القنيطرة\"}],\"province\":[{\"language\":\"eng\",\"value\":\"Kenitra\"},{\"language\":\"ara\",\"value\":\"القنيطرة\"}],\"city\":[{\"language\":\"eng\",\"value\":\"Mograne\"},{\"language\":\"ara\",\"value\":\"مڭرن\"}],\"postalCode\":\"123456\",\"phone\":\"9962385854\",\"email\":\"raghavdce@gmail.com\",\"localAdministrativeAuthority\":[{\"language\":\"eng\",\"value\":\"14023\"},{\"language\":\"ara\",\"value\":\"14023\"}],\"proofOfAddress\":{\"value\":\"POA_Rental contract\",\"type\":\"Rental contract\",\"format\":\"jpg\"},\"proofOfIdentity\":{\"value\":\"POI_CNIE card\",\"type\":\"CNIE card\",\"format\":\"jpg\"},\"proofOfRelationship\":{\"value\":\"POR_Certificate of Relationship\",\"type\":\"Certificate of Relationship\",\"format\":\"jpg\"},\"individualBiometrics\":{\"format\":\"cbeff\",\"version\":1,\"value\":\"applicant_bio_CBEFF\"},\"IDSchemaVersion\":1,\"CNIENumber\":\"12345678809\"}}";

		registrationStatusDto = new InternalRegistrationStatusDto();
		registrationStatusDto.setRegistrationId("10003100030001120190410111048");

		Mockito.when(utility.getRegistrationProcessorMappingJson()).thenReturn(JsonUtil.getJSONObject(JsonUtil.objectMapperReadValue(mappingJsonString, JSONObject.class), MappingJsonConstants.IDENTITY));
		
		Mockito.when(adapter.getFile(any(), any(),anyString())).thenReturn(inputStream);

		PowerMockito.mockStatic(IOUtils.class);
		PowerMockito.when(IOUtils.class, "toByteArray", inputStream).thenReturn(idJsonString.getBytes());

		PowerMockito.mockStatic(Utilities.class);
		PowerMockito.when(Utilities.class, "getJson", any(), any()).thenReturn(mappingJsonString);
	}

	@Test
	public void mandatoryValidationSuccessTest() throws IOException, io.mosip.kernel.core.exception.IOException, io.mosip.kernel.packetmanager.exception.PacketDecryptionFailureException, ApiNotAccessibleException {

		boolean result = mandatoryValidation.mandatoryFieldValidation(registrationStatusDto.getRegistrationId(), source, packetValidationDto);
		assertTrue("Test for mandate fields", result);
	}

	@Test
	public void mandatoryValidationMissingFieldFailureTest() throws Exception {
		// Removing fullname field from ID json
		idJsonString = "{\"identity\":{\"dateOfBirth\":\"1999/01/01\",\"age\":20,\"gender\":[{\"language\":\"eng\",\"value\":\"Male\"},{\"language\":\"ara\",\"value\":\"الذكر\"}],\"residenceStatus\":[{\"language\":\"eng\",\"value\":\"Non-Foreigner\"},{\"language\":\"ara\",\"value\":\"غير أجنبي\"}],\"addressLine1\":[{\"language\":\"eng\",\"value\":\"Kumar street\"},{\"language\":\"ara\",\"value\":\"نعةشق سفقثثف\"}],\"addressLine3\":[{\"language\":\"eng\",\"value\":\"Line 3\"},{\"language\":\"ara\",\"value\":\"مىث ٣\"}],\"region\":[{\"language\":\"eng\",\"value\":\"Rabat Sale Kenitra\"},{\"language\":\"ara\",\"value\":\"جهة الرباط سلا القنيطرة\"}],\"province\":[{\"language\":\"eng\",\"value\":\"Kenitra\"},{\"language\":\"ara\",\"value\":\"القنيطرة\"}],\"city\":[{\"language\":\"eng\",\"value\":\"Mograne\"},{\"language\":\"ara\",\"value\":\"مڭرن\"}],\"postalCode\":\"123456\",\"phone\":\"9962385854\",\"email\":\"raghavdce@gmail.com\",\"localAdministrativeAuthority\":[{\"language\":\"eng\",\"value\":\"14023\"},{\"language\":\"ara\",\"value\":\"14023\"}],\"proofOfAddress\":{\"value\":\"POA_Rental contract\",\"type\":\"Rental contract\",\"format\":\"jpg\"},\"proofOfIdentity\":{\"value\":\"POI_CNIE card\",\"type\":\"CNIE card\",\"format\":\"jpg\"},\"proofOfRelationship\":{\"value\":\"POR_Certificate of Relationship\",\"type\":\"Certificate of Relationship\",\"format\":\"jpg\"},\"individualBiometrics\":{\"format\":\"cbeff\",\"version\":1,\"value\":\"applicant_bio_CBEFF\"},\"IDSchemaVersion\":1,\"CNIENumber\":\"12345678809\"}}";
		PowerMockito.mockStatic(IOUtils.class);
		PowerMockito.when(IOUtils.class, "toByteArray", inputStream).thenReturn(idJsonString.getBytes());
		boolean result = mandatoryValidation.mandatoryFieldValidation(registrationStatusDto.getRegistrationId(), source, packetValidationDto);
		assertFalse("Test for mandatory missing fields", result);
	}

	@Test
	public void mandatoryValidationMarkMandatoryFalseTest() throws Exception {
		// Mark mandatory field false for fullName from IdentityMapping json
		ClassLoader classLoader = getClass().getClassLoader();
		File identityMappingjson = new File(classLoader.getResource("RegistrationProcessorIdentity.json").getFile());
		InputStream identityMappingjsonStream = new FileInputStream(identityMappingjson);
		String identityMappingjsonString = "";
		try {
			identityMappingjsonString = IOUtils.toString(identityMappingjsonStream, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		boolean result = mandatoryValidation.mandatoryFieldValidation(registrationStatusDto.getRegistrationId(), source, packetValidationDto);
		assertTrue("Test for mandate field marked false", result);
	}

	@Test
	public void mandatoryValidationNullOrEmptyValueTest() throws Exception {
		// null or empty value for mandate field
		idJsonString = "{\"identity\":{\"fullName\":\"\",\"dateOfBirth\":null,\"age\":20,\"gender\":[{\"language\":\"eng\",\"value\":\"Male\"},{\"language\":\"ara\",\"value\":\"الذكر\"}],\"residenceStatus\":[{\"language\":\"eng\",\"value\":\"Non-Foreigner\"},{\"language\":\"ara\",\"value\":\"غير أجنبي\"}],\"addressLine1\":[{\"language\":\"eng\",\"value\":\"Kumar street\"},{\"language\":\"ara\",\"value\":\"نعةشق سفقثثف\"}],\"addressLine3\":[{\"language\":\"eng\",\"value\":\"Line 3\"},{\"language\":\"ara\",\"value\":\"مىث ٣\"}],\"region\":[{\"language\":\"eng\",\"value\":\"Rabat Sale Kenitra\"},{\"language\":\"ara\",\"value\":\"جهة الرباط سلا القنيطرة\"}],\"province\":[{\"language\":\"eng\",\"value\":\"Kenitra\"},{\"language\":\"ara\",\"value\":\"القنيطرة\"}],\"city\":[{\"language\":\"eng\",\"value\":\"Mograne\"},{\"language\":\"ara\",\"value\":\"مڭرن\"}],\"postalCode\":\"123456\",\"phone\":\"9962385854\",\"email\":\"raghavdce@gmail.com\",\"localAdministrativeAuthority\":[{\"language\":\"eng\",\"value\":\"14023\"},{\"language\":\"ara\",\"value\":\"14023\"}],\"proofOfAddress\":{\"value\":\"POA_Rental contract\",\"type\":\"Rental contract\",\"format\":\"jpg\"},\"proofOfIdentity\":{\"value\":\"POI_CNIE card\",\"type\":\"CNIE card\",\"format\":\"jpg\"},\"proofOfRelationship\":{\"value\":\"POR_Certificate of Relationship\",\"type\":\"Certificate of Relationship\",\"format\":\"jpg\"},\"individualBiometrics\":{\"format\":\"cbeff\",\"version\":1,\"value\":\"applicant_bio_CBEFF\"},\"IDSchemaVersion\":1,\"CNIENumber\":\"12345678809\"}}";
		PowerMockito.mockStatic(IOUtils.class);
		PowerMockito.when(IOUtils.class, "toByteArray", inputStream).thenReturn(idJsonString.getBytes());
		boolean result = mandatoryValidation.mandatoryFieldValidation(registrationStatusDto.getRegistrationId(), source, packetValidationDto);
		assertFalse("Test for mandatory missing fields", result);
	}

	@Test
	public void mandatoryValidationNullOrEmptyValueInIDJsonTest() throws Exception {
		// null or empty value for mandate field from ID JSON value object
		idJsonString = "{\"identity\":{\"fullName\":[{\"language\":\"eng\",\"value\":null},{\"language\":\"ara\",\"value\":\"قشلشرثىيقشى ر\"}],\"dateOfBirth\":\"1999/01/01\",\"age\":20,\"gender\":[{\"language\":\"eng\",\"value\":\"Male\"},{\"language\":\"ara\",\"value\":\"الذكر\"}],\"residenceStatus\":[{\"language\":\"eng\",\"value\":\"Non-Foreigner\"},{\"language\":\"ara\",\"value\":\"غير أجنبي\"}],\"addressLine1\":[{\"language\":\"eng\",\"value\":\"Kumar street\"},{\"language\":\"ara\",\"value\":\"نعةشق سفقثثف\"}],\"addressLine3\":[{\"language\":\"eng\",\"value\":\"Line 3\"},{\"language\":\"ara\",\"value\":\"مىث ٣\"}],\"region\":[{\"language\":\"eng\",\"value\":\"Rabat Sale Kenitra\"},{\"language\":\"ara\",\"value\":\"جهة الرباط سلا القنيطرة\"}],\"province\":[{\"language\":\"eng\",\"value\":\"Kenitra\"},{\"language\":\"ara\",\"value\":\"القنيطرة\"}],\"city\":[{\"language\":\"eng\",\"value\":\"Mograne\"},{\"language\":\"ara\",\"value\":\"مڭرن\"}],\"postalCode\":\"123456\",\"phone\":\"9962385854\",\"email\":\"raghavdce@gmail.com\",\"localAdministrativeAuthority\":[{\"language\":\"eng\",\"value\":\"14023\"},{\"language\":\"ara\",\"value\":\"14023\"}],\"proofOfAddress\":{\"value\":\"POA_Rental contract\",\"type\":\"Rental contract\",\"format\":\"jpg\"},\"proofOfIdentity\":{\"value\":\"POI_CNIE card\",\"type\":\"CNIE card\",\"format\":\"jpg\"},\"proofOfRelationship\":{\"value\":\"POR_Certificate of Relationship\",\"type\":\"Certificate of Relationship\",\"format\":\"jpg\"},\"individualBiometrics\":{\"format\":\"cbeff\",\"version\":1,\"value\":\"applicant_bio_CBEFF\"},\"IDSchemaVersion\":1,\"CNIENumber\":\"12345678809\"}}";
		PowerMockito.mockStatic(IOUtils.class);
		PowerMockito.when(IOUtils.class, "toByteArray", inputStream).thenReturn(idJsonString.getBytes());
		boolean result = mandatoryValidation.mandatoryFieldValidation(registrationStatusDto.getRegistrationId(), source, packetValidationDto);
		assertFalse("Test for mandatory missing fields", result);
	}
}