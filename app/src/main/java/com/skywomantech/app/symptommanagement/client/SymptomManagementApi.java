package com.skywomantech.app.symptommanagement.client;

import com.skywomantech.app.symptommanagement.data.*;

import java.util.Collection;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * This interface defines an API for the Symptom Management Service. The
 * interface is used to provide a contract for client/server interactions. 
 * The interface is annotated with Retrofit annotations for the benefit of clients
 */
public interface SymptomManagementApi {
	
	public static final String PATIENT_PATH = "/patient";
	public static final String PHYSICIAN_PATH = "/physician";
	public static final String MEDICATION_PATH = "/medication";
	
	public static final String NAME_PARAMETER = "name";
	public static final String ID_PATH = "/{id}";
	public static final String ID_PARAMETER = "id";
	
	public static final String SEARCH_PATH = "/find";
	public static final String PATIENT_SEARCH_PATH = PATIENT_PATH + SEARCH_PATH;
	public static final String PHYSICIAN_SEARCH_PATH = PHYSICIAN_PATH + SEARCH_PATH;
	public static final String MEDICATION_SEARCH_PATH = MEDICATION_PATH + SEARCH_PATH;
	
	//TODO: How to handle the images - storing and downloading?  Figure out later!

	@GET(PATIENT_PATH)
	public Collection<Patient> getAllPatients();
	
	@GET(PATIENT_PATH+ID_PATH)
	public Patient getPatient(@Path(ID_PARAMETER) long userId);		
	
	@POST(PATIENT_PATH)
	public Patient addPatient(@Body Patient patient);
	
	@PUT(PATIENT_PATH+ID_PATH)
	public Patient updatePatient(@Path(ID_PARAMETER) long userId, @Body Patient patient);		

	@DELETE(PATIENT_PATH+ID_PATH)
	public void deletePatient(@Path(ID_PARAMETER) long userId);		
	
	@GET(PATIENT_SEARCH_PATH)
	public Collection<Patient> findByPatientName(@Query(NAME_PARAMETER) String name);
	
	@GET(PHYSICIAN_PATH)
	public Collection<Physician> getAllPhysicians();
	
	@GET(PHYSICIAN_PATH+ID_PATH)
	public Physician getPhysician(@Path(ID_PARAMETER) long userId);
	
	@POST(PHYSICIAN_PATH)
	public Physician addPhysician(@Body Physician physician);
	
	@PUT(PHYSICIAN_PATH+ID_PATH)
	public Physician updatePhysician(@Path(ID_PARAMETER) long userId, @Body Physician physician);	
	
	@DELETE(PHYSICIAN_PATH+ID_PATH)
	public void deletePhysician(@Path(ID_PARAMETER) long userId);
	
	@GET(PHYSICIAN_SEARCH_PATH)
	public Collection<Physician> findByPhysicianName(@Query(NAME_PARAMETER) String name);
	
	@GET(MEDICATION_PATH)
	public Collection<Medication> getAllMedications();
	
	@GET(MEDICATION_PATH+ID_PATH)
	public Medication getMedication(@Path(ID_PARAMETER) long medId);
	
	@POST(MEDICATION_PATH)
	public Medication addMedication(@Body Medication medication);
	
	@PUT(MEDICATION_PATH+ID_PATH)
	public Medication updateMedication(@Path(ID_PARAMETER) long medId, @Body Medication medication);	
	
	@DELETE(MEDICATION_PATH+ID_PATH)
	public void deleteMedication(@Path(ID_PARAMETER) long medId);
	
	@GET(MEDICATION_SEARCH_PATH)
	public Collection<Medication> findByMedicationName(@Query(NAME_PARAMETER) String name);
}
