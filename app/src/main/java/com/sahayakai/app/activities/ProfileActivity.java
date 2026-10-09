package com.sahayakai.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.sahayakai.app.R;
import com.sahayakai.app.helpers.SupabaseHelper;
import com.sahayakai.app.models.User;

/**
 * ProfileActivity — collects and saves the user's complete profile.
 *
 * HOW IT WORKS:
 * - User fills in their personal details (age, state, income, education, etc.).
 * - We save this to Supabase under profiles table.
 * - Profile is used by the chatbot to give personalized scheme recommendations.
 * - If the user already has a profile (Edit mode), we pre-fill all fields.
 */
public class ProfileActivity extends AppCompatActivity {

    // Input fields
    private TextInputEditText etName, etAge;

    // Dropdown spinners
    private Spinner spinnerGender, spinnerState, spinnerDistrict, spinnerIncome, spinnerCategory,
            spinnerEducation, spinnerCourse, spinnerOccupation, spinnerStudent;

    private Button btnSave, btnLogout;
    private ProgressBar progressBar;

    private SupabaseHelper supabase;
    private boolean isEditMode = false;

    // Dropdown options
    private final String[] GENDERS = {"Select Gender", "Male", "Female", "Other", "Prefer not to say"};
    private final String[] INCOMES = {
            "Select Income Range",
            "Below ₹1 lakh",
            "₹1-3 lakh",
            "₹3-5 lakh",
            "₹5-8 lakh",
            "₹8-12 lakh",
            "Above ₹12 lakh"
    };
    private final String[] CATEGORIES = {
            "Select Category", "General", "OBC", "SC", "ST", "Minority", "EWS", "Other"
    };
    private final String[] EDUCATIONS = {
            "Select Education",
            "Below Class 10",
            "Class 10 (Matriculation)",
            "Class 12 (Intermediate)",
            "Diploma / ITI",
            "Undergraduate (B.Tech/B.Sc/BA/B.Com etc.)",
            "Post Graduate (M.Tech/M.Sc/MBA etc.)",
            "PhD / Research",
            "Other"
    };
    private final String[] COURSES = {
            "Select Course / Field of Study",
            "School Education (Class 1-10)",
            "Arts & Humanities (11th/12th/BA/MA)",
            "Science (Non-Medical - PCM / B.Sc)",
            "Medical & Healthcare (PCB / MBBS / Nursing / Pharmacy)",
            "Commerce & Accounts (B.Com / M.Com / CA / BBA)",
            "Engineering & Technology (B.Tech / BE / M.Tech / Diploma)",
            "Computer Science & IT (BCA / MCA / Software / Data)",
            "Business & Management (BBA / MBA / Marketing)",
            "Agriculture & Farming Sciences",
            "Law & Legal Studies (LLB / LLM)",
            "Vocational / Skill Training (ITI / Polytechnic)",
            "Hotel Management & Tourism",
            "Journalism & Mass Media",
            "Teaching & Education (B.Ed / D.El.Ed / M.Ed)",
            "Design & Fine Arts (Fashion / Graphic / Animation)",
            "Civil Services / Competitive Exam Prep",
            "Not Applicable / None",
            "Other"
    };
    private final String[] OCCUPATIONS = {
            "Select Occupation",
            "Student",
            "Unemployed / Job Seeker",
            "Farmer / Agriculture Worker",
            "Daily Wage Worker / Laborer",
            "Self-Employed / Freelancer",
            "Small Business Owner / Shopkeeper",
            "Private Sector Employee",
            "Government / PSU Employee",
            "Gig Worker (Delivery / Driver / Cab)",
            "Homemaker",
            "Retired",
            "Other"
    };
    private final String[] STUDENT_OPTIONS = {"Currently a Student?", "Yes", "No"};
    private final String[] STATES = {
            "Select State",
            "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
            "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka",
            "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram",
            "Nagaland", "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu",
            "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal",
            "Delhi", "Jammu & Kashmir", "Ladakh", "Chandigarh", "Other"
    };

    // State-wise districts map
    private static final Map<String, String[]> STATE_DISTRICTS = new HashMap<>();
    static {
        STATE_DISTRICTS.put("Uttar Pradesh", new String[]{"Select District","Agra","Aligarh","Allahabad","Ambedkar Nagar","Amethi","Amroha","Auraiya","Azamgarh","Baghpat","Bahraich","Ballia","Balrampur","Banda","Barabanki","Bareilly","Basti","Bijnor","Budaun","Bulandshahr","Chandauli","Chitrakoot","Deoria","Etah","Etawah","Faizabad","Farrukhabad","Fatehpur","Firozabad","Gautam Buddha Nagar","Ghaziabad","Ghazipur","Gonda","Gorakhpur","Hamirpur","Hapur","Hardoi","Hathras","Jalaun","Jaunpur","Jhansi","Kannauj","Kanpur Dehat","Kanpur Nagar","Kasganj","Kaushambi","Kushinagar","Lakhimpur Kheri","Lalitpur","Lucknow","Maharajganj","Mahoba","Mainpuri","Mathura","Mau","Meerut","Mirzapur","Moradabad","Muzaffarnagar","Pilibhit","Pratapgarh","Raebareli","Rampur","Saharanpur","Sambhal","Sant Kabir Nagar","Shahjahanpur","Shamli","Shravasti","Siddharthnagar","Sitapur","Sonbhadra","Sultanpur","Unnao","Varanasi"});
        STATE_DISTRICTS.put("Bihar", new String[]{"Select District","Araria","Arwal","Aurangabad","Banka","Begusarai","Bhagalpur","Bhojpur","Buxar","Darbhanga","Gaya","Gopalganj","Jamui","Jehanabad","Kaimur","Katihar","Khagaria","Kishanganj","Lakhisarai","Madhepura","Madhubani","Munger","Muzaffarpur","Nalanda","Nawada","Patna","Purnia","Rohtas","Saharsa","Samastipur","Saran","Sheikhpura","Sheohar","Sitamarhi","Siwan","Supaul","Vaishali","West Champaran","East Champaran"});
        STATE_DISTRICTS.put("Maharashtra", new String[]{"Select District","Ahmednagar","Akola","Amravati","Aurangabad","Beed","Bhandara","Buldhana","Chandrapur","Dhule","Gadchiroli","Gondia","Hingoli","Jalgaon","Jalna","Kolhapur","Latur","Mumbai City","Mumbai Suburban","Nagpur","Nanded","Nandurbar","Nashik","Osmanabad","Palghar","Parbhani","Pune","Raigad","Ratnagiri","Sangli","Satara","Sindhudurg","Solapur","Thane","Wardha","Washim","Yavatmal"});
        STATE_DISTRICTS.put("Rajasthan", new String[]{"Select District","Ajmer","Alwar","Banswara","Baran","Barmer","Bharatpur","Bhilwara","Bikaner","Bundi","Chittorgarh","Churu","Dausa","Dholpur","Dungarpur","Ganganagar","Hanumangarh","Jaipur","Jaisalmer","Jalore","Jhalawar","Jhunjhunu","Jodhpur","Karauli","Kota","Nagaur","Pali","Pratapgarh","Rajsamand","Sawai Madhopur","Sikar","Sirohi","Tonk","Udaipur"});
        STATE_DISTRICTS.put("Madhya Pradesh", new String[]{"Select District","Agar Malwa","Alirajpur","Anuppur","Ashoknagar","Balaghat","Barwani","Betul","Bhind","Bhopal","Burhanpur","Chhatarpur","Chhindwara","Damoh","Datia","Dewas","Dhar","Dindori","Guna","Gwalior","Harda","Hoshangabad","Indore","Jabalpur","Jhabua","Katni","Khandwa","Khargone","Mandla","Mandsaur","Morena","Narsinghpur","Neemuch","Panna","Raisen","Rajgarh","Ratlam","Rewa","Sagar","Satna","Sehore","Seoni","Shahdol","Shajapur","Sheopur","Shivpuri","Sidhi","Singrauli","Tikamgarh","Ujjain","Umaria","Vidisha"});
        STATE_DISTRICTS.put("Gujarat", new String[]{"Select District","Ahmedabad","Amreli","Anand","Aravalli","Banaskantha","Bharuch","Bhavnagar","Botad","Chhota Udaipur","Dahod","Dang","Devbhoomi Dwarka","Gandhinagar","Gir Somnath","Jamnagar","Junagadh","Kheda","Kutch","Mahisagar","Mehsana","Morbi","Narmada","Navsari","Panchmahal","Patan","Porbandar","Rajkot","Sabarkantha","Surat","Surendranagar","Tapi","Vadodara","Valsad"});
        STATE_DISTRICTS.put("Karnataka", new String[]{"Select District","Bagalkot","Ballari","Belagavi","Bengaluru Rural","Bengaluru Urban","Bidar","Chamarajanagar","Chikkaballapur","Chikkamagaluru","Chitradurga","Dakshina Kannada","Davanagere","Dharwad","Gadag","Hassan","Haveri","Kalaburagi","Kodagu","Kolar","Koppal","Mandya","Mysuru","Raichur","Ramanagara","Shivamogga","Tumakuru","Udupi","Uttara Kannada","Vijayapura","Yadgir"});
        STATE_DISTRICTS.put("Tamil Nadu", new String[]{"Select District","Ariyalur","Chengalpattu","Chennai","Coimbatore","Cuddalore","Dharmapuri","Dindigul","Erode","Kallakurichi","Kancheepuram","Karur","Krishnagiri","Madurai","Mayiladuthurai","Nagapattinam","Namakkal","Nilgiris","Perambalur","Pudukkottai","Ramanathapuram","Ranipet","Salem","Sivaganga","Tenkasi","Thanjavur","Theni","Thoothukudi","Tiruchirappalli","Tirunelveli","Tirupathur","Tiruppur","Tiruvallur","Tiruvannamalai","Tiruvarur","Vellore","Viluppuram","Virudhunagar"});
        STATE_DISTRICTS.put("West Bengal", new String[]{"Select District","Alipurduar","Bankura","Birbhum","Cooch Behar","Dakshin Dinajpur","Darjeeling","Hooghly","Howrah","Jalpaiguri","Jhargram","Kalimpong","Kolkata","Malda","Murshidabad","Nadia","North 24 Parganas","Paschim Bardhaman","Paschim Medinipur","Purba Bardhaman","Purba Medinipur","Purulia","South 24 Parganas","Uttar Dinajpur"});
        STATE_DISTRICTS.put("Punjab", new String[]{"Select District","Amritsar","Barnala","Bathinda","Faridkot","Fatehgarh Sahib","Fazilka","Firozpur","Gurdaspur","Hoshiarpur","Jalandhar","Kapurthala","Ludhiana","Mansa","Moga","Muktsar","Nawanshahr","Pathankot","Patiala","Rupnagar","Sangrur","SAS Nagar","Tarn Taran"});
        STATE_DISTRICTS.put("Haryana", new String[]{"Select District","Ambala","Bhiwani","Charkhi Dadri","Faridabad","Fatehabad","Gurugram","Hisar","Jhajjar","Jind","Kaithal","Karnal","Kurukshetra","Mahendragarh","Nuh","Palwal","Panchkula","Panipat","Rewari","Rohtak","Sirsa","Sonipat","Yamunanagar"});
        STATE_DISTRICTS.put("Andhra Pradesh", new String[]{"Select District","Alluri Sitharama Raju","Anakapalli","Anantapur","Annamayya","Bapatla","Chittoor","Dr. B.R. Ambedkar Konaseema","East Godavari","Eluru","Guntur","Kakinada","Krishna","Kurnool","Nandyal","NTR","Palnadu","Prakasam","Sri Potti Sri Ramulu Nellore","Sri Sathya Sai","Srikakulam","Tirupati","Visakhapatnam","Vizianagaram","West Godavari","YSR"});
        STATE_DISTRICTS.put("Telangana", new String[]{"Select District","Adilabad","Bhadradri Kothagudem","Hanumakonda","Hyderabad","Jagtial","Jangaon","Jayashankar Bhupalpally","Jogulamba Gadwal","Kamareddy","Karimnagar","Khammam","Komaram Bheem","Mahabubabad","Mahabubnagar","Mancherial","Medak","Medchal Malkajgiri","Mulugu","Nagarkurnool","Nalgonda","Narayanpet","Nirmal","Nizamabad","Peddapalli","Rajanna Sircilla","Rangareddy","Sangareddy","Siddipet","Suryapet","Vikarabad","Wanaparthy","Warangal","Yadadri Bhuvanagiri"});
        STATE_DISTRICTS.put("Odisha", new String[]{"Select District","Angul","Balangir","Balasore","Bargarh","Bhadrak","Boudh","Cuttack","Deogarh","Dhenkanal","Gajapati","Ganjam","Jagatsinghpur","Jajpur","Jharsuguda","Kalahandi","Kandhamal","Kendrapara","Kendujhar","Khordha","Koraput","Malkangiri","Mayurbhanj","Nabarangpur","Nayagarh","Nuapada","Puri","Rayagada","Sambalpur","Subarnapur","Sundargarh"});
        STATE_DISTRICTS.put("Assam", new String[]{"Select District","Bajali","Baksa","Barpeta","Biswanath","Bongaigaon","Cachar","Charaideo","Chirang","Darrang","Dhemaji","Dhubri","Dibrugarh","Dima Hasao","Goalpara","Golaghat","Hailakandi","Hojai","Jorhat","Kamrup","Kamrup Metropolitan","Karbi Anglong","Karimganj","Kokrajhar","Lakhimpur","Majuli","Morigaon","Nagaon","Nalbari","Sivasagar","Sonitpur","South Salmara-Mankachar","Tinsukia","Udalguri","West Karbi Anglong"});
        STATE_DISTRICTS.put("Jharkhand", new String[]{"Select District","Bokaro","Chatra","Deoghar","Dhanbad","Dumka","East Singhbhum","Garhwa","Giridih","Godda","Gumla","Hazaribagh","Jamtara","Khunti","Koderma","Latehar","Lohardaga","Pakur","Palamu","Ramgarh","Ranchi","Sahebganj","Seraikela Kharsawan","Simdega","West Singhbhum"});
        STATE_DISTRICTS.put("Chhattisgarh", new String[]{"Select District","Balod","Baloda Bazar","Balrampur","Bastar","Bemetara","Bijapur","Bilaspur","Dantewada","Dhamtari","Durg","Gariaband","Gaurela-Pendra-Marwahi","Janjgir-Champa","Jashpur","Kabirdham","Kanker","Kondagaon","Korba","Koriya","Mahasamund","Manendragarh","Mohla-Manpur","Mungeli","Narayanpur","Raigarh","Raipur","Rajnandgaon","Sakti","Sarangarh-Bilaigarh","Sukma","Surajpur","Surguja"});
        STATE_DISTRICTS.put("Uttarakhand", new String[]{"Select District","Almora","Bageshwar","Chamoli","Champawat","Dehradun","Haridwar","Nainital","Pauri Garhwal","Pithoragarh","Rudraprayag","Tehri Garhwal","Udham Singh Nagar","Uttarkashi"});
        STATE_DISTRICTS.put("Himachal Pradesh", new String[]{"Select District","Bilaspur","Chamba","Hamirpur","Kangra","Kinnaur","Kullu","Lahaul and Spiti","Mandi","Shimla","Sirmaur","Solan","Una"});
        STATE_DISTRICTS.put("Kerala", new String[]{"Select District","Alappuzha","Ernakulam","Idukki","Kannur","Kasaragod","Kollam","Kottayam","Kozhikode","Malappuram","Palakkad","Pathanamthitta","Thiruvananthapuram","Thrissur","Wayanad"});
        STATE_DISTRICTS.put("Delhi", new String[]{"Select District","Central Delhi","East Delhi","New Delhi","North Delhi","North East Delhi","North West Delhi","Shahdara","South Delhi","South East Delhi","South West Delhi","West Delhi"});
        STATE_DISTRICTS.put("Goa", new String[]{"Select District","North Goa","South Goa"});
        STATE_DISTRICTS.put("Jammu & Kashmir", new String[]{"Select District","Anantnag","Bandipora","Baramulla","Budgam","Doda","Ganderbal","Jammu","Kathua","Kishtwar","Kulgam","Kupwara","Poonch","Pulwama","Rajouri","Ramban","Reasi","Samba","Shopian","Srinagar","Udhampur"});
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        supabase = SupabaseHelper.getInstance();

        // If user is not logged in, redirect straight to Login screen
        if (!supabase.isLoggedIn()) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
            return;
        }

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Check if we are in edit mode (coming from Profile tab in HomeActivity)
        isEditMode = getIntent().getBooleanExtra("editMode", false);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(isEditMode ? "Edit Profile" : "Complete Your Profile");
            if (isEditMode) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        // Initialize views
        initViews();
        setupSpinners();

        if (isEditMode) {
            // Load existing profile to pre-fill
            loadExistingProfile();
        }

        btnSave.setOnClickListener(v -> saveProfile());

        // Logout button — always visible so user can switch accounts or re-login anytime
        btnLogout.setVisibility(android.view.View.VISIBLE);
        btnLogout.setText(isEditMode ? "Logout" : "Back to Login / Switch Account");
        btnLogout.setOnClickListener(v -> logout());
    }

    private void initViews() {
        etName = findViewById(R.id.et_name);
        etAge = findViewById(R.id.et_age);
        spinnerCourse = findViewById(R.id.spinner_course);
        spinnerOccupation = findViewById(R.id.spinner_occupation);
        spinnerGender = findViewById(R.id.spinner_gender);
        spinnerState = findViewById(R.id.spinner_state);
        spinnerDistrict = findViewById(R.id.spinner_district);
        spinnerIncome = findViewById(R.id.spinner_income);
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerEducation = findViewById(R.id.spinner_education);
        spinnerStudent = findViewById(R.id.spinner_student);
        btnSave = findViewById(R.id.btn_save_profile);
        btnLogout = findViewById(R.id.btn_logout);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupSpinners() {
        setSpinnerAdapter(spinnerGender, GENDERS);
        setSpinnerAdapter(spinnerState, STATES);
        setSpinnerAdapter(spinnerDistrict, new String[]{"Select State First"});
        setSpinnerAdapter(spinnerIncome, INCOMES);
        setSpinnerAdapter(spinnerCategory, CATEGORIES);
        setSpinnerAdapter(spinnerEducation, EDUCATIONS);
        setSpinnerAdapter(spinnerCourse, COURSES);
        setSpinnerAdapter(spinnerOccupation, OCCUPATIONS);
        setSpinnerAdapter(spinnerStudent, STUDENT_OPTIONS);

        // When state changes, update districts dropdown
        spinnerState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedState = STATES[position];
                String[] districts = STATE_DISTRICTS.getOrDefault(
                        selectedState, new String[]{"Select District", "Other"});
                setSpinnerAdapter(spinnerDistrict, districts);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setSpinnerAdapter(Spinner spinner, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void loadExistingProfile() {
        setLoading(true);
        supabase.getProfile(new SupabaseHelper.OnProfileCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    setLoading(false);
                    if (user != null) {
                        populateForm(user);
                    }
                });
            }
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(ProfileActivity.this, "Could not load profile: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void populateForm(User user) {
        if (user.getName() != null) etName.setText(user.getName());
        if (user.getAge() != null) etAge.setText(user.getAge());
        setSpinnerValue(spinnerGender, GENDERS, user.getGender());
        // Set state first so district dropdown gets populated via listener
        setSpinnerValue(spinnerState, STATES, user.getState());
        // After state is set, restore district selection
        if (user.getState() != null && user.getDistrict() != null) {
            String[] districts = STATE_DISTRICTS.getOrDefault(
                    user.getState(), new String[]{"Select District", "Other"});
            setSpinnerAdapter(spinnerDistrict, districts);
            setSpinnerValue(spinnerDistrict, districts, user.getDistrict());
        }
        setSpinnerValue(spinnerIncome, INCOMES, user.getIncome());
        setSpinnerValue(spinnerCategory, CATEGORIES, user.getCategory());
        setSpinnerValue(spinnerEducation, EDUCATIONS, user.getEducation());
        setSpinnerValue(spinnerCourse, COURSES, user.getCourse());
        setSpinnerValue(spinnerOccupation, OCCUPATIONS, user.getOccupation());
        setSpinnerValue(spinnerStudent, STUDENT_OPTIONS, user.getStudentStatus());
    }

    private void setSpinnerValue(Spinner spinner, String[] items, String value) {
        if (value == null) return;
        for (int i = 0; i < items.length; i++) {
            if (items[i].equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void saveProfile() {
        // Collect values
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String age = etAge.getText() != null ? etAge.getText().toString().trim() : "";
        String district = spinnerDistrict.getSelectedItemPosition() > 0 ?
                spinnerDistrict.getSelectedItem().toString() : "";
        String course = spinnerCourse.getSelectedItemPosition() > 0 ?
                spinnerCourse.getSelectedItem().toString() : "";
        String occupation = spinnerOccupation.getSelectedItemPosition() > 0 ?
                spinnerOccupation.getSelectedItem().toString() : "";
        String gender = spinnerGender.getSelectedItemPosition() > 0 ?
                spinnerGender.getSelectedItem().toString() : "";
        String state = spinnerState.getSelectedItemPosition() > 0 ?
                spinnerState.getSelectedItem().toString() : "";
        String income = spinnerIncome.getSelectedItemPosition() > 0 ?
                spinnerIncome.getSelectedItem().toString() : "";
        String category = spinnerCategory.getSelectedItemPosition() > 0 ?
                spinnerCategory.getSelectedItem().toString() : "";
        String education = spinnerEducation.getSelectedItemPosition() > 0 ?
                spinnerEducation.getSelectedItem().toString() : "";
        String studentStatus = spinnerStudent.getSelectedItemPosition() > 0 ?
                spinnerStudent.getSelectedItem().toString() : "";

        // Validate required fields
        if (TextUtils.isEmpty(name)) {
            android.util.Log.e("SahayakAI", "Validation: Name is empty");
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(age)) {
            android.util.Log.e("SahayakAI", "Validation: Age is empty");
            etAge.setError("Age is required");
            etAge.requestFocus();
            return;
        }
        try {
            int ageNum = Integer.parseInt(age);
            if (ageNum < 1 || ageNum > 120) {
                android.util.Log.e("SahayakAI", "Validation: Invalid age: " + age);
                etAge.setError("Please enter a valid age (1-120)");
                etAge.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            android.util.Log.e("SahayakAI", "Validation: Age is not a number: " + age);
            etAge.setError("Age must be a number");
            etAge.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(gender)) {
            android.util.Log.e("SahayakAI", "Validation: Gender is empty");
            Toast.makeText(this, "Please select Gender from the dropdown", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(state)) {
            android.util.Log.e("SahayakAI", "Validation: State is empty");
            Toast.makeText(this, "Please select your State from the dropdown", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build User object
        User user = new User();
        user.setName(name);
        user.setAge(age);
        user.setGender(gender);
        user.setState(state);
        user.setDistrict(district);
        user.setIncome(income);
        user.setCategory(category);
        user.setEducation(education);
        user.setCourse(course);
        user.setOccupation(occupation);
        user.setStudentStatus(studentStatus);
        user.setEmail(supabase.getCurrentUserEmail());

        android.util.Log.d("SahayakAI", "Saving profile for UID=" + supabase.getCurrentUserId() + ", email=" + supabase.getCurrentUserEmail());
        setLoading(true);

        supabase.saveProfile(user, new SupabaseHelper.OnCompleteCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(ProfileActivity.this, "Profile saved successfully! 🎉", Toast.LENGTH_SHORT).show();
                    // Go to Home screen
                    startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
                    finish();
                });
            }
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    setLoading(false);
                    android.util.Log.e("SahayakAI", "saveProfile failure callback: " + error);
                    if (error != null && error.toLowerCase().contains("not logged in")) {
                        new android.app.AlertDialog.Builder(ProfileActivity.this)
                                .setTitle("Login Required")
                                .setMessage("Your session is not active. Please log in to save your profile.")
                                .setPositiveButton("Go to Login", (d, w) -> {
                                    SupabaseHelper.getInstance().clearSession();
                                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                })
                                .setCancelable(false)
                                .show();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Save Failed: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!loading);
        btnSave.setText(loading ? "Saving..." : "Save Profile");
    }

    private void logout() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    // Clear Supabase session (removes stored JWT token)
                    SupabaseHelper.getInstance().clearSession();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
