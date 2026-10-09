package com.sahayakai.app.models;

/**
 * User model — stores profile information.
 * This is what gets saved to Firebase under users/{UID}
 */
public class User {

    private String uid;
    private String name;
    private String email;
    private String age;
    private String gender;
    private String state;
    private String district;
    private String income;       // e.g. "Below ₹1 lakh", "₹1-3 lakh", etc.
    private String category;     // General, OBC, SC, ST, Minority, etc.
    private String education;    // Class 10, Class 12, Graduate, Post Graduate, etc.
    private String course;       // B.Tech, B.Sc, MBA, etc.
    private String occupation;   // Student, Farmer, Unemployed, Salaried, etc.
    private String studentStatus; // "Yes" or "No"

    // Required no-argument constructor for Firebase deserialization
    public User() {}

    public User(String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;
    }

    // ---- Getters and Setters ----

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getIncome() { return income; }
    public void setIncome(String income) { this.income = income; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }

    public String getStudentStatus() { return studentStatus; }
    public void setStudentStatus(String studentStatus) { this.studentStatus = studentStatus; }

    /**
     * Returns a text summary of the user profile for use in Gemini prompts.
     */
    public String toProfileSummary() {
        return "Name: " + name + "\n" +
                "Age: " + age + "\n" +
                "Gender: " + gender + "\n" +
                "State: " + state + "\n" +
                "District: " + district + "\n" +
                "Annual Family Income: " + income + "\n" +
                "Category: " + category + "\n" +
                "Education: " + education + "\n" +
                "Course: " + course + "\n" +
                "Occupation: " + occupation + "\n" +
                "Currently a Student: " + studentStatus;
    }
}
