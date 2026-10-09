package com.sahayakai.app.helpers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

/**
 * SchemeDatabaseHelper — populates Supabase with demo scheme data.
 *
 * ⚠️ IMPORTANT: All data here is for ACADEMIC DEMONSTRATION PURPOSES ONLY.
 * Information is simplified and may not reflect actual government schemes.
 * Always verify from official government portals.
 *
 * HOW IT WORKS (for viva explanation):
 * - Called once when the app first runs and the schemes table is empty.
 * - Sends 15+ scheme objects to Supabase via REST API (UPSERT).
 * - Running multiple times is safe — UPSERT ignores duplicate IDs.
 */
public class SchemeDatabaseHelper {

    /**
     * Populates Supabase with demo schemes.
     * Called from HomeActivity when schemes table is empty.
     * @param supabase  The SupabaseHelper instance (needed for REST calls)
     */
    public static void populateSchemesToSupabase(SupabaseHelper supabase) {
        addScheme(supabase, "scheme_001",
                "⚠️ [DEMO] PM Scholarship Scheme",
                "Education",
                "Central Government scholarship for wards of ex-servicemen and ex-Coast Guard personnel pursuing professional courses.",
                "Age: 18-25 years | India | Student: Yes | Courses: Engineering, Medical, MBA, MCA, etc. | Minimum 60% in Class 12",
                Arrays.asList(
                        "₹2,000 per month for boys",
                        "₹2,250 per month for girls",
                        "Renewable annually based on performance"
                ),
                Arrays.asList(
                        "Aadhaar Card",
                        "Ex-serviceman/Coast Guard PPO",
                        "Class 12 Marksheet",
                        "Bonafide Certificate",
                        "Bank Account Details",
                        "Passport Size Photo"
                ),
                Arrays.asList(
                        "Visit the official DESW portal",
                        "Register with Aadhaar number",
                        "Upload required documents",
                        "Submit application before deadline",
                        "Track status on portal"
                ),
                "31 October 2026",
                "https://ksb.gov.in",
                "https://ksb.gov.in/new/apply.html",
                "https://ksb.gov.in/scholarship.html"
        );

        addScheme(supabase, "scheme_002",
                "⚠️ [DEMO] NSP Post-Matric Scholarship (SC Students)",
                "Education",
                "National Scholarship Portal scholarship for Scheduled Caste students pursuing post-matric education.",
                "Category: SC | Age: 18-35 years | India | Studying at post-matric level | Annual income below ₹2.5 lakh",
                Arrays.asList(
                        "Maintenance allowance ₹500-₹1,200/month",
                        "Reimbursement of compulsory fees",
                        "Book grant and study tour charges"
                ),
                Arrays.asList(
                        "Aadhaar Card",
                        "Caste Certificate (SC)",
                        "Income Certificate",
                        "Previous year marksheet",
                        "Bonafide Certificate",
                        "Bank Account (linked with Aadhaar)"
                ),
                Arrays.asList(
                        "Register on National Scholarship Portal (scholarships.gov.in)",
                        "Login and fill the application form",
                        "Upload scanned documents",
                        "Submit and note registration number",
                        "Institute verifies the application"
                ),
                "31 December 2026",
                "https://scholarships.gov.in",
                "https://scholarships.gov.in/fresh/loginPage",
                "https://scholarships.gov.in/public/home"
        );

        addScheme(supabase, "scheme_003",
                "⚠️ [DEMO] Beti Bachao Beti Padhao",
                "Women",
                "Government initiative to address declining child sex ratio and promote education of the girl child across India.",
                "Gender: Female | Age: 0-18 years | India | Focus on girls education and welfare",
                Arrays.asList(
                        "Financial assistance for girl child education",
                        "Awareness campaigns and community programs",
                        "Sukanya Samriddhi Account benefits",
                        "Health and nutrition support"
                ),
                Arrays.asList(
                        "Girl child's Aadhaar Card",
                        "Birth Certificate",
                        "Parents' Aadhaar Card",
                        "Bank Account (for financial benefits)"
                ),
                Arrays.asList(
                        "Visit nearest Anganwadi center or District office",
                        "Fill the application form",
                        "Submit documents",
                        "Open Sukanya Samriddhi Account at post office/bank"
                ),
                "Ongoing",
                "https://wcd.nic.in",
                "https://wcd.nic.in/bbbp-schemes",
                "https://wcd.nic.in/bbbp-schemes"
        );

        addScheme(supabase, "scheme_004",
                "⚠️ [DEMO] PM Kaushal Vikas Yojana (PMKVY)",
                "Skill Development",
                "Flagship skill development scheme enabling youth to take up industry-relevant skill training and earn recognition.",
                "Age: 18-35 years | India | Minimum: Class 10 passed | Unemployed or seeking skill upgradation",
                Arrays.asList(
                        "Free skill training in 300+ courses",
                        "Government certification recognized by industry",
                        "Financial reward of ₹8,000 on completion",
                        "Placement assistance"
                ),
                Arrays.asList(
                        "Aadhaar Card",
                        "Class 10 Marksheet",
                        "Bank Account Details",
                        "Passport Size Photo"
                ),
                Arrays.asList(
                        "Find PMKVY training center at pmkvyofficial.org",
                        "Register online or visit center",
                        "Enroll in chosen skill course",
                        "Complete training and assessment",
                        "Receive certificate and reward"
                ),
                "31 March 2027",
                "https://pmkvyofficial.org",
                "https://pmkvyofficial.org/find-a-training-centre",
                "https://pmkvyofficial.org/about"
        );

        addScheme(supabase, "scheme_005",
                "⚠️ [DEMO] Ayushman Bharat (PM-JAY)",
                "Healthcare",
                "World's largest government health insurance scheme providing coverage of ₹5 lakh per family per year for secondary and tertiary care.",
                "Annual family income: Below ₹3 lakh | India | SECC database beneficiaries | BPL families",
                Arrays.asList(
                        "Health cover of ₹5 lakh per family per year",
                        "Cashless treatment at empaneled hospitals",
                        "Covers 1,500+ medical procedures",
                        "No restriction on family size or age"
                ),
                Arrays.asList(
                        "Aadhaar Card",
                        "Ration Card",
                        "Income Certificate",
                        "Domicile Certificate"
                ),
                Arrays.asList(
                        "Check eligibility at pmjay.gov.in",
                        "Locate nearest Common Service Center",
                        "Submit documents for Ayushman Card",
                        "Receive Ayushman Bharat Health Card",
                        "Use card at any empaneled hospital"
                ),
                "Ongoing",
                "https://pmjay.gov.in",
                "https://mera.pmjay.gov.in",
                "https://pmjay.gov.in/about/pm-jayseyhat"
        );

        addScheme(supabase, "scheme_006",
                "⚠️ [DEMO] PM Awas Yojana - Gramin (Housing)",
                "Housing",
                "Provides financial assistance to rural below-poverty-line households to construct their own homes.",
                "India | Rural area resident | BPL / SECC listed | No pucca house | Annual income below ₹1 lakh",
                Arrays.asList(
                        "₹1.2 lakh financial assistance (plain areas)",
                        "₹1.3 lakh for hilly/difficult areas",
                        "Toilet construction under Swachh Bharat Mission",
                        "MGNREGA labor wages for construction"
                ),
                Arrays.asList(
                        "Aadhaar Card",
                        "BPL Certificate or SECC ID",
                        "Bank Account Details",
                        "Land Documents"
                ),
                Arrays.asList(
                        "Check eligibility in SECC/Awaas+ app",
                        "Contact nearest Gram Panchayat",
                        "Submit application with documents",
                        "Wait for official approval",
                        "Receive installment on construction progress"
                ),
                "Ongoing",
                "https://pmayg.nic.in",
                "https://pmayg.nic.in",
                "https://pmayg.nic.in/netiay/Uploaded/pmayGurbanGuidelines.pdf"
        );

        addScheme(supabase, "scheme_007",
                "⚠️ [DEMO] PM Kisan Samman Nidhi",
                "Agriculture",
                "Provides income support of ₹6,000 per year to all land-holding farmer families in India.",
                "India | Occupation: Farmer | Owns agricultural land | Not an income taxpayer",
                Arrays.asList(
                        "₹6,000 per year in three equal installments of ₹2,000",
                        "Direct transfer to bank account",
                        "Available to all eligible farmer families"
                ),
                Arrays.asList(
                        "Aadhaar Card",
                        "Land Records / Khasra-Khatauni",
                        "Bank Account (Aadhaar linked)",
                        "Mobile Number linked to Aadhaar"
                ),
                Arrays.asList(
                        "Visit pmkisan.gov.in",
                        "Click on 'Farmer Corner' → New Registration",
                        "Enter Aadhaar number and state",
                        "Fill land and bank details",
                        "Submit — verify on portal after few days"
                ),
                "Ongoing",
                "https://pmkisan.gov.in",
                "https://pmkisan.gov.in/RegistrationFormLanguage.aspx",
                "https://pmkisan.gov.in/Documents/PM-KISANGuidelines.pdf"
        );

        addScheme(supabase, "scheme_008",
                "⚠️ [DEMO] Mudra Loan (PMMY) - Shishu",
                "Entrepreneurship",
                "Provides collateral-free loans to micro and small enterprises, startups, and self-employed individuals.",
                "Age: 18-65 years | India | Starting or expanding a micro business | Not a defaulter",
                Arrays.asList(
                        "Shishu: Loans up to ₹50,000",
                        "Kishore: Loans up to ₹5 lakh",
                        "Tarun: Loans up to ₹10 lakh",
                        "No collateral required",
                        "Subsidized interest rates"
                ),
                Arrays.asList(
                        "Aadhaar Card",
                        "PAN Card",
                        "Business Plan / Proposal",
                        "Bank Statements (6 months)",
                        "Passport Size Photo",
                        "Address Proof"
                ),
                Arrays.asList(
                        "Visit mudra.org.in to find nearest bank/NBFC",
                        "Prepare business plan document",
                        "Visit selected bank with documents",
                        "Fill Mudra loan application form",
                        "Submit and receive Mudra Card if approved"
                ),
                "Ongoing",
                "https://mudra.org.in",
                "https://mudra.org.in/bank_detail.aspx",
                "https://mudra.org.in/Content/pdf/Mudra_Annual_Report_2022-23.pdf"
        );

        addScheme(supabase, "scheme_009",
                "⚠️ [DEMO] Jan Dhan Yojana (PMJDY)",
                "Financial Assistance",
                "Provides access to financial services including bank accounts, remittances, credit, insurance and pension.",
                "Age: 10+ years | India | No existing bank account | BPL / Low income",
                Arrays.asList(
                        "Zero balance savings account",
                        "Rupay debit card",
                        "₹1 lakh accident insurance cover",
                        "₹30,000 life insurance cover",
                        "Overdraft facility up to ₹10,000"
                ),
                Arrays.asList(
                        "Aadhaar Card",
                        "Voter ID (alternate ID if no Aadhaar)",
                        "Passport Size Photo"
                ),
                Arrays.asList(
                        "Visit any bank branch or Business Correspondent (BC)",
                        "Fill account opening form (Form A)",
                        "Submit Aadhaar and photo",
                        "Account opened same day",
                        "Rupay card mailed within 2 weeks"
                ),
                "Ongoing",
                "https://pmjdy.gov.in",
                "https://pmjdy.gov.in/scheme",
                "https://pmjdy.gov.in/scheme"
        );

        addScheme(supabase, "scheme_010",
                "⚠️ [DEMO] Sukanya Samriddhi Yojana",
                "Women",
                "Small deposit savings scheme for girl children to secure their education and marriage expenses.",
                "Gender: Female | Age: 0-10 years (account opening) | India | Maximum 2 girl children per family",
                Arrays.asList(
                        "High interest rate (currently ~8.2% p.a.)",
                        "Tax benefits under Section 80C",
                        "Partial withdrawal at age 18 for education",
                        "Account matures when girl turns 21"
                ),
                Arrays.asList(
                        "Girl child's Birth Certificate",
                        "Parent/Guardian Aadhaar Card",
                        "Address Proof",
                        "Passport Size Photo of girl and guardian",
                        "Initial deposit (minimum ₹250)"
                ),
                Arrays.asList(
                        "Visit nearest Post Office or authorized bank",
                        "Fill SSY account opening form",
                        "Submit documents and initial deposit",
                        "Receive passbook",
                        "Deposit minimum ₹250 per year to keep active"
                ),
                "Ongoing",
                "https://www.india.gov.in/sukanya-samriddhi-yojna",
                "https://www.indiapost.gov.in/Financial/Pages/Content/SSA.aspx",
                "https://www.india.gov.in/sukanya-samriddhi-yojna"
        );

        addScheme(supabase, "scheme_011",
                "⚠️ [DEMO] PM Rozgar Protsahan Yojana (PMRPY)",
                "Employment",
                "Incentivizes employers to generate new employment by paying EPF contribution for new employees.",
                "Age: 18-35 years | India | New formal employment | Wage: Below ₹15,000/month",
                Arrays.asList(
                        "Government pays 12% employer EPF contribution",
                        "Encourages formal employment generation",
                        "Benefit for 3 years per new employee",
                        "Available to all EPFO-registered establishments"
                ),
                Arrays.asList(
                        "Aadhaar Card",
                        "EPFO Universal Account Number (UAN)",
                        "Bank Account Details",
                        "Employment offer letter"
                ),
                Arrays.asList(
                        "Employer registers on pmrpy.gov.in",
                        "Links establishment with EPFO",
                        "New employees registered on portal",
                        "Government contribution credited automatically",
                        "Track on PMRPY dashboard"
                ),
                "31 March 2027",
                "https://pmrpy.gov.in",
                "https://pmrpy.gov.in",
                "https://pmrpy.gov.in/about_pmrpy.html"
        );

        addScheme(supabase, "scheme_012",
                "⚠️ [DEMO] MGNREGS (Mahatma Gandhi NREGS)",
                "Employment",
                "Guarantees 100 days of wage employment per financial year to rural households.",
                "India | Rural resident | Age: 18+ | Occupation: Unskilled manual work | Any category",
                Arrays.asList(
                        "100 days guaranteed wage employment per year",
                        "Minimum wage as per state rate",
                        "Work within 5 km of residence",
                        "If work not provided within 15 days — unemployment allowance"
                ),
                Arrays.asList(
                        "Aadhaar Card",
                        "Job Card (issued by Gram Panchayat)",
                        "Bank Account Details"
                ),
                Arrays.asList(
                        "Visit nearest Gram Panchayat office",
                        "Apply for Job Card with Aadhaar",
                        "Receive Job Card within 15 days",
                        "Request for work at Gram Panchayat",
                        "Work allocated within 15 days of request"
                ),
                "Ongoing",
                "https://nrega.nic.in",
                "https://nrega.nic.in/netnrega/home.aspx",
                "https://nrega.nic.in/netnrega/mgnrega_new/Nrega_home.aspx"
        );

        addScheme(supabase, "scheme_013",
                "⚠️ [DEMO] Startup India Scheme",
                "Entrepreneurship",
                "Flagship initiative to build a strong startup ecosystem and drive economic growth and employment.",
                "Age: 18-45 years | India | Registered Startup (< 10 years old) | Annual turnover < ₹100 crore",
                Arrays.asList(
                        "Tax exemption for 3 years",
                        "Self-certification under 9 labor/environmental laws",
                        "₹10,000 crore Fund of Funds support",
                        "Fast-track patent examination",
                        "Access to government tenders"
                ),
                Arrays.asList(
                        "Business Registration Certificate (Pvt. Ltd/LLP/Partnership)",
                        "PAN Card of company",
                        "Aadhaar/PAN of founder",
                        "Proof of concept/product",
                        "Recommendation letter (if applicable)"
                ),
                Arrays.asList(
                        "Register at startupindia.gov.in",
                        "Login and click 'Register your Startup'",
                        "Fill startup details and upload documents",
                        "Submit for DPIIT recognition",
                        "Receive DPIIT recognition certificate"
                ),
                "Ongoing",
                "https://startupindia.gov.in",
                "https://www.startupindia.gov.in/content/sih/en/startupgov/startup-recognition-page.html",
                "https://startupindia.gov.in/content/sih/en/government-schemes.html"
        );

        addScheme(supabase, "scheme_014",
                "⚠️ [DEMO] Mahila Shakti Kendra",
                "Women",
                "Provides convergence of various schemes for women at district and block level. Empowers rural women.",
                "Gender: Female | India | Rural area | Any age | Any income",
                Arrays.asList(
                        "Skill training and vocational education",
                        "Digital literacy programs",
                        "Nutrition and health awareness",
                        "Access to government schemes",
                        "Community mobilization support"
                ),
                Arrays.asList(
                        "Aadhaar Card",
                        "Address Proof",
                        "Passport Size Photo"
                ),
                Arrays.asList(
                        "Visit nearest Mahila Shakti Kendra or block office",
                        "Register for training programs",
                        "Participate in skill development sessions",
                        "Access government scheme linkages"
                ),
                "Ongoing",
                "https://wcd.nic.in",
                "https://wcd.nic.in/schemes/mahila-shakti-kendras",
                "https://wcd.nic.in/schemes/mahila-shakti-kendras"
        );

        addScheme(supabase, "scheme_015",
                "⚠️ [DEMO] NSP Pre-Matric Scholarship (Minority Students)",
                "Education",
                "Scholarship for students from minority communities studying in Class 1 to Class 10.",
                "Category: Minority | Class 1-10 | India | Annual income below ₹1 lakh | Minimum 50% marks",
                Arrays.asList(
                        "Day scholars: ₹1,000 per year",
                        "Hostellers: ₹3,000 per year",
                        "Ad-hoc grant for books and stationery"
                ),
                Arrays.asList(
                        "Aadhaar Card",
                        "Minority Community Certificate",
                        "Income Certificate",
                        "Previous year marksheet",
                        "Bonafide Certificate from school"
                ),
                Arrays.asList(
                        "Register on scholarships.gov.in",
                        "Select 'Pre-Matric Scholarship for Minority'",
                        "Fill application and upload documents",
                        "School verification required",
                        "Renewal required every year"
                ),
                "31 October 2026",
                "https://scholarships.gov.in",
                "https://scholarships.gov.in/fresh/loginPage",
                "https://scholarships.gov.in/public/home"
        );

        // --- 10 ADDITIONAL SCHEMES ---

        addScheme(supabase, "scheme_016",
                "⚠️ [DEMO] OBC Post-Matric Scholarship",
                "Education",
                "National Scholarship Portal scholarship for Other Backward Class students for post-matric education.",
                "Category: OBC | Age: 18-35 years | India | Post-matric student | Annual income below ₹1 lakh",
                Arrays.asList(
                        "Maintenance allowance ₹300-₹750/month",
                        "Reimbursement of tuition and other fees",
                        "Book grant included"
                ),
                Arrays.asList(
                        "Aadhaar Card",
                        "OBC Certificate (Non-Creamy Layer)",
                        "Income Certificate",
                        "Previous year marksheet",
                        "Bonafide Certificate",
                        "Bank Account (Aadhaar linked)"
                ),
                Arrays.asList(
                        "Register on scholarships.gov.in",
                        "Select 'Post-Matric Scholarship for OBC'",
                        "Upload documents",
                        "Institute verification required",
                        "Scholarship disbursed to bank account"
                ),
                "31 December 2026",
                "https://scholarships.gov.in",
                "https://scholarships.gov.in/fresh/loginPage",
                "https://scholarships.gov.in/public/home"
        );

        addScheme(supabase, "scheme_017",
                "⚠️ [DEMO] PM Ujjwala Yojana (Free LPG)",
                "Women",
                "Provides free LPG connections to women from below-poverty-line and deprived households.",
                "Gender: Female | India | BPL household | No existing LPG connection | Annual income below ₹1 lakh",
                Arrays.asList(
                        "Free LPG connection (deposit waived)",
                        "First cylinder free",
                        "Reduces household air pollution",
                        "Empowers women with clean cooking fuel"
                ),
                Arrays.asList(
                        "Aadhaar Card",
                        "BPL Card / Ration Card",
                        "Bank Account Details",
                        "Address Proof",
                        "Passport Size Photo"
                ),
                Arrays.asList(
                        "Visit nearest LPG distributor (HP/Bharat/Indane)",
                        "Fill PMUY application form",
                        "Submit BPL card and Aadhaar",
                        "Connection issued within 7 days",
                        "First cylinder provided free"
                ),
                "Ongoing",
                "https://pmuy.gov.in",
                "https://pmuy.gov.in/ujjwala2.html",
                "https://pmuy.gov.in/about_pmuy.html"
        );

        addScheme(supabase, "scheme_018",
                "⚠️ [DEMO] Indira Gandhi National Disability Pension",
                "Financial Assistance",
                "Monthly pension for persons with severe disabilities (80%+ disability) from BPL families.",
                "India | Disability: 80% or more | BPL household | Age: 18+ years",
                Arrays.asList(
                        "₹300/month central contribution",
                        "Additional state contribution (varies)",
                        "Direct transfer to bank account",
                        "No age limit for severely disabled"
                ),
                Arrays.asList(
                        "Aadhaar Card",
                        "Disability Certificate (80%+ from govt hospital)",
                        "BPL Card / Ration Card",
                        "Bank Account Details",
                        "Address Proof"
                ),
                Arrays.asList(
                        "Contact District Social Welfare Officer",
                        "Fill IGNDPS application form",
                        "Attach disability and BPL certificate",
                        "Submit to Gram Panchayat or Block Office",
                        "Approved pension credited monthly"
                ),
                "Ongoing",
                "https://nsap.nic.in",
                "https://nsap.nic.in",
                "https://nsap.nic.in/guidlines.do"
        );

        addScheme(supabase, "scheme_019",
                "⚠️ [DEMO] Kanya Sumangala Yojana (UP)",
                "Women",
                "Uttar Pradesh government scheme for financial support to girl children from birth to graduation.",
                "Gender: Female | State: Uttar Pradesh | Age: 0-21 years | Annual income below ₹3 lakh | Max 2 daughters per family",
                Arrays.asList(
                        "₹2,000 at birth",
                        "₹1,000 on first vaccination",
                        "₹2,000 on Class 1 admission",
                        "₹2,000 on Class 6 admission",
                        "₹3,000 on Class 9 admission",
                        "₹5,000 on Class 12 or graduation"
                ),
                Arrays.asList(
                        "Girl child's Aadhaar Card",
                        "Birth Certificate",
                        "Parents' Aadhaar Card",
                        "Income Certificate (below ₹3 lakh)",
                        "Bank Account (in girl's name)",
                        "Address Proof (UP)"
                ),
                Arrays.asList(
                        "Apply at mksy.up.gov.in",
                        "Register and login on portal",
                        "Fill application with girl's details",
                        "Upload required documents",
                        "Submit for district verification"
                ),
                "Ongoing",
                "https://mksy.up.gov.in",
                "https://mksy.up.gov.in",
                "https://mksy.up.gov.in"
        );

        addScheme(supabase, "scheme_020",
                "⚠️ [DEMO] PM POSHAN (Mid-Day Meal)",
                "Education",
                "Provides free nutritious meals to children in government and aided schools to improve attendance and nutrition.",
                "Age: 6-14 years | Enrolled in govt/aided school | India | Class 1-8 students",
                Arrays.asList(
                        "Free hot cooked meal every school day",
                        "Improved school attendance",
                        "Nutritional support for children",
                        "Reduces dropout rates"
                ),
                Arrays.asList(
                        "School enrollment proof",
                        "Aadhaar Card of child"
                ),
                Arrays.asList(
                        "No application needed \u2014 automatic for enrolled students",
                        "Child must be enrolled in Govt/aided school Class 1-8",
                        "Meals provided daily during school days"
                ),
                "Ongoing",
                "https://pmposhan.education.gov.in",
                "https://pmposhan.education.gov.in",
                "https://pmposhan.education.gov.in"
        );

        addScheme(supabase, "scheme_021",
                "⚠️ [DEMO] Stand Up India Scheme",
                "Entrepreneurship",
                "Provides bank loans between ₹10 lakh and ₹1 crore to SC/ST and women entrepreneurs for greenfield enterprises.",
                "Category: SC or ST, OR Gender: Female | Age: 18+ | India | Starting a new business (greenfield) | Not a defaulter",
                Arrays.asList(
                        "Loan amount: ₹10 lakh to ₹1 crore",
                        "Repayment period up to 7 years",
                        "Moratorium period of 18 months",
                        "Covers manufacturing, services, or trading"
                ),
                Arrays.asList(
                        "Aadhaar Card",
                        "PAN Card",
                        "Business Plan",
                        "SC/ST Certificate (if applicable)",
                        "Bank Statements (6 months)",
                        "Address Proof"
                ),
                Arrays.asList(
                        "Register at standupmitra.in",
                        "Find nearest bank branch on portal",
                        "Submit business plan and documents",
                        "Bank appraises the project",
                        "Loan disbursed upon approval"
                ),
                "Ongoing",
                "https://standupmitra.in",
                "https://standupmitra.in",
                "https://standupmitra.in/Home/SUISchemes"
        );

        addScheme(supabase, "scheme_022",
                "⚠️ [DEMO] Atal Pension Yojana (APY)",
                "Financial Assistance",
                "Government-backed pension scheme for unorganized sector workers, guaranteeing pension of ₹1,000-₹5,000/month after age 60.",
                "Age: 18-40 years | India | Bank account holder | Not an income taxpayer | Not covered under EPF/NPS",
                Arrays.asList(
                        "Guaranteed pension ₹1,000-₹5,000/month after 60",
                        "Government co-contributes 50% (for eligible subscribers)",
                        "Tax benefits under Section 80CCD",
                        "Death benefit for nominee"
                ),
                Arrays.asList(
                        "Aadhaar Card",
                        "Bank Account with mobile number",
                        "Age proof (18-40 years)"
                ),
                Arrays.asList(
                        "Visit your bank branch or net banking",
                        "Fill APY registration form",
                        "Choose pension amount (₹1,000-₹5,000/month)",
                        "Set up auto-debit from bank account",
                        "Get PRAN (Permanent Retirement Account Number)"
                ),
                "Ongoing",
                "https://npscra.nsdl.co.in/scheme-details.php",
                "https://enps.nsdl.com/eNPS/NationalPensionSystem.html",
                "https://npscra.nsdl.co.in/scheme-details.php"
        );

        addScheme(supabase, "scheme_023",
                "⚠️ [DEMO] National Apprenticeship Promotion Scheme",
                "Employment",
                "Promotes apprenticeship training in industries by sharing stipend cost. Provides on-the-job training with stipend.",
                "Age: 14-21 years | India | Minimum Class 8 passed | Looking for vocational/technical training | Any category",
                Arrays.asList(
                        "Monthly stipend during apprenticeship",
                        "Government shares 25% of stipend with industry",
                        "National Apprenticeship Certificate on completion",
                        "Hands-on industry experience"
                ),
                Arrays.asList(
                        "Aadhaar Card",
                        "Educational certificates (Class 8 minimum)",
                        "Bank Account Details",
                        "Passport Size Photo"
                ),
                Arrays.asList(
                        "Register at apprenticeshipindia.org",
                        "Search for available apprenticeship positions",
                        "Apply to relevant establishment",
                        "Get selected and sign apprenticeship contract",
                        "Start training and receive stipend monthly"
                ),
                "Ongoing",
                "https://apprenticeshipindia.org",
                "https://apprenticeshipindia.org/candidate/register",
                "https://apprenticeshipindia.org"
        );

        addScheme(supabase, "scheme_024",
                "⚠️ [DEMO] PM Solar Rooftop Scheme",
                "Financial Assistance",
                "Subsidy for installing solar panels on rooftops of residential and institutional buildings to reduce electricity bills.",
                "India | Home/building owner | Grid-connected electricity connection | Any income | Any category",
                Arrays.asList(
                        "40% subsidy for up to 2 kW capacity",
                        "20% subsidy for 2-3 kW capacity",
                        "Saves up to ₹1,500-₹18,000 per year on electricity",
                        "Excess power sold back to grid (net metering)"
                ),
                Arrays.asList(
                        "Aadhaar Card",
                        "Electricity Bill (6 months)",
                        "Property Ownership Documents",
                        "Bank Account Details",
                        "Passport Size Photo"
                ),
                Arrays.asList(
                        "Apply on pmsuryaghar.gov.in",
                        "Enter electricity consumer number",
                        "Select empanelled vendor",
                        "Get solar panels installed",
                        "Submit net metering application to DISCOM",
                        "Subsidy credited to bank account"
                ),
                "Ongoing",
                "https://pmsuryaghar.gov.in",
                "https://pmsuryaghar.gov.in",
                "https://pmsuryaghar.gov.in/consumerRegistration"
        );

        addScheme(supabase, "scheme_025",
                "⚠️ [DEMO] Digital Saksharta Abhiyan (DISHA)",
                "Skill Development",
                "Provides basic digital literacy training to rural household members to enable participation in the digital economy.",
                "India | Rural area | Age: 14-60 years | Not digitally literate | Any income | Any category",
                Arrays.asList(
                        "Free digital literacy training (20 hours)",
                        "Certificate recognized by Government of India",
                        "Covers smartphone, internet, banking apps, government portals",
                        "Training in Hindi and regional languages"
                ),
                Arrays.asList(
                        "Aadhaar Card",
                        "Passport Size Photo"
                ),
                Arrays.asList(
                        "Visit nearest Common Service Center (CSC)",
                        "Register for DISHA training",
                        "Attend 20-hour training program",
                        "Appear for online assessment",
                        "Download digital literacy certificate"
                ),
                "Ongoing",
                "https://pmgdisha.in",
                "https://www.pmgdisha.in/student/register",
                "https://pmgdisha.in/about-us"
        );
    }

    /**
     * Legacy method name kept for compatibility — now delegates to Supabase version.
     * @deprecated Use populateSchemesToSupabase(SupabaseHelper) instead.
     */
    public static void populateDemoSchemes() {
        // No-op: Cannot populate without SupabaseHelper instance.
        // HomeActivity now calls populateSchemesToSupabase(supabase) directly.
    }

    // ============================================================
    // Helper to add a scheme to Supabase
    // ============================================================
    private static void addScheme(
            SupabaseHelper supabase,
            String id, String name, String category, String eligibility, String summary,
            List<String> benefits, List<String> documents,
            List<String> steps, String deadline,
            String officialLink, String applyLink, String guidelinesLink) {

        try {
            JSONObject scheme = new JSONObject();
            scheme.put("id", id);
            scheme.put("name", name);
            scheme.put("category", category);
            scheme.put("summary", summary);
            scheme.put("eligibility", eligibility);
            scheme.put("deadline", deadline);
            scheme.put("official_link", officialLink);
            scheme.put("apply_link", applyLink);
            scheme.put("guidelines_link", guidelinesLink);
            scheme.put("benefits", new JSONArray(benefits));
            scheme.put("documents", new JSONArray(documents));
            scheme.put("application_steps", new JSONArray(steps));

            supabase.upsertScheme(scheme, null); // Fire-and-forget
        } catch (JSONException e) {
            // Log error but don't crash
            android.util.Log.e("SchemeDatabaseHelper", "Failed to build scheme JSON: " + e.getMessage());
        }
    }
}
