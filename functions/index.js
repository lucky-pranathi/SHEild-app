const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

setGlobalOptions({ maxInstances: 10 });

exports.createUserByAdmin = functions.https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'Request had no auth.');
    }

    const callerUid = context.auth.uid;
    const roleSnap = await admin.database().ref(`/users/${callerUid}/role`).once('value');
    const callerRole = roleSnap.val();
    if (callerRole !== 'admin') {
        throw new functions.https.HttpsError('permission-denied', 'Only admin can create users.');
    }

    const email = data.email;
    const password = data.password;
    const role = data.role || 'user';
    if (!email || !password) {
        throw new functions.https.HttpsError('invalid-argument', 'Email and password are required.');
    }

    try {
        const userRecord = await admin.auth().createUser({ email: email, password: password });
        await admin.database().ref(`/users/${userRecord.uid}`).set({ role: role, displayName: "" });
        return { message: `User ${email} created`, uid: userRecord.uid };
    } catch (err) {
        throw new functions.https.HttpsError('internal', err.message);
    }
});
