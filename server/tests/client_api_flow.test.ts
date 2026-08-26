import { initDatabase, getDatabase } from '../src/db/database.js';
import { api } from '../../client/src/api/client.js'; // Wait, let's just make direct fetch requests to the running backend on port 5000 so we test the real network interface!

async function runClientAPIFlowTest() {
  console.log('\n======================================================');
  console.log('🧪 RUNNING CAREERPILOT CLIENT-API INTEGRATION TEST');
  console.log('======================================================\n');

  const BASE_URL = 'http://localhost:5000/api';
  const testEmail = `client_user_${Date.now()}@careerpilot.io`;
  const testPassword = 'SecurePassword123!';
  const testName = 'Client Test User';

  // 1. SIGNUP
  console.log('1. Attempting Sign Up...');
  const signupRes = await fetch(`${BASE_URL}/auth/signup`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email: testEmail, password: testPassword, fullName: testName })
  });

  if (!signupRes.ok) {
    throw new Error(`Signup failed: ${signupRes.status} ${await signupRes.text()}`);
  }

  const signupData = await signupRes.json();
  const token = signupData.token;
  console.log('✅ Sign Up successful! Received Auth Token.');

  const authHeaders = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  };

  // 2. FETCH DASHBOARD
  console.log('\n2. Fetching Dashboard Analytics...');
  const dashboardRes = await fetch(`${BASE_URL}/analytics/dashboard`, {
    headers: authHeaders
  });

  if (!dashboardRes.ok) {
    throw new Error(`Dashboard fetch failed: ${dashboardRes.status} ${await dashboardRes.text()}`);
  }

  const dashboardData = await dashboardRes.json();
  console.log(`✅ Dashboard fetched. Initial readiness score: ${dashboardData.analytics.readinessScore}%`);

  // 3. TRACK TELEMETRY EVENT
  console.log('\n3. Tracking Custom Telemetry Event...');
  const eventRes = await fetch(`${BASE_URL}/analytics/event`, {
    method: 'POST',
    headers: authHeaders,
    body: JSON.stringify({
      eventName: 'test_interactive_event',
      eventData: { client: 'API test script', status: 'active' }
    })
  });

  if (!eventRes.ok) {
    throw new Error(`Track event failed: ${eventRes.status} ${await eventRes.text()}`);
  }

  console.log('✅ Telemetry event tracked successfully.');

  // 4. UPDATE PROFILE
  console.log('\n4. Updating User Profile...');
  const profileRes = await fetch(`${BASE_URL}/profile`, {
    method: 'PUT',
    headers: authHeaders,
    body: JSON.stringify({
      location: 'San Francisco, CA',
      target_role: 'Senior Staff Engineer'
    })
  });

  if (!profileRes.ok) {
    throw new Error(`Profile update failed: ${profileRes.status} ${await profileRes.text()}`);
  }

  console.log('✅ Profile updated (Location: San Francisco, CA; Target Role: Senior Staff Engineer).');

  // 5. VERIFY UPDATE ON DASHBOARD
  console.log('\n5. Re-fetching Dashboard to verify Profile target role updates...');
  const verifyRes = await fetch(`${BASE_URL}/analytics/dashboard`, {
    headers: authHeaders
  });

  const verifyData = await verifyRes.json();
  if (verifyData.analytics.targetRole !== 'Senior Staff Engineer') {
    throw new Error(`Verification failed. Expected target role "Senior Staff Engineer", got "${verifyData.analytics.targetRole}"`);
  }

  console.log(`✅ Verified target role update in dashboard data: "${verifyData.analytics.targetRole}"`);

  console.log('\n======================================================');
  console.log('🎉 CLIENT-API INTEGRATION FLOW TEST PASSED 100%');
  console.log('======================================================\n');
}

runClientAPIFlowTest().catch((err) => {
  console.error('\n❌ CLIENT-API FLOW TEST FAILED:', err);
  process.exit(1);
});
