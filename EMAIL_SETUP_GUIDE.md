# Real Email API Setup Guide

## Option 1: Brevo (Recommended - FREE 300 emails/day)

### Step 1: Sign Up
1. Go to https://www.brevo.com/
2. Click "Sign up free"
3. Fill in your details and verify your email

### Step 2: Get API Key
1. Log in to https://app.brevo.com/
2. Go to: Settings > SMTP & API > API Keys
3. Or direct link: https://app.brevo.com/settings/keys/api
4. Click "Generate a new API key"
5. Give it a name (e.g., "RealEstateHub")
6. Copy the API key (starts with `xkeysib-`)

### Step 3: Configure Your Code
1. Open: `WebService_RealsEstateHub/src/main/java/ch/unil/doplab/webservice_realsestatehub/EmailNotificationService.java`

2. Replace this line:
```java
private static final String BREVO_API_KEY = "YOUR_BREVO_API_KEY_HERE";
```

With your actual API key:
```java
private static final String BREVO_API_KEY = "xkeysib-abc123...";
```

3. Enable real API:
```java
private static final boolean USE_REAL_API = true;
```

### Step 4: Rebuild and Deploy
```bash
cd WebService_RealsEstateHub
mvn clean package
cd ..
docker compose restart
```

### Step 5: Test
Update an offer status in Postman, and you'll receive a REAL email at nikhilesh.acharya@unil.ch!

---

## Option 2: SendGrid (Alternative)

### Step 1: Sign Up
1. Go to https://sendgrid.com/
2. Sign up for free (100 emails/day)
3. Verify your email

### Step 2: Get API Key
1. Go to Settings > API Keys
2. Create API key with "Mail Send" permissions
3. Copy the key

### Step 3: Use SendGrid
You'll need to modify the code to use SendGrid's API format.

---

## Current Setup (Simulated)

Currently, your app is set to **simulated mode**:
- `USE_REAL_API = false`
- Emails are printed to Docker logs
- Perfect for demo/testing without API setup

To see simulated emails:
```bash
docker logs -f payara-RealEstateHub
```

---

## For Your Demo

**Option A: Simulated (Current)**
✅ No setup required
✅ Shows external API integration
✅ Fast to demonstrate
✅ Professors see logs, not inbox

**Option B: Real Emails (Brevo)**
✅ Actually sends real emails
✅ More impressive
⚠️ Requires 5 minutes setup
⚠️ Needs internet connection

**Recommendation:** Stick with simulated for demo, or quickly set up Brevo if you want to impress! 🚀
