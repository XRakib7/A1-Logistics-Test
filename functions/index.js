// functions/index.js
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendPickupNotification = functions.firestore
    .document('PickupRequests/{orderId}')
    .onCreate(async (snapshot, context) => {
        const pickupData = snapshot.data();

        // Get all admin tokens
        const adminsSnapshot = await admin.firestore().collection('Admins').get();
        const tokens = [];

        adminsSnapshot.forEach(doc => {
            if (doc.data().fcmToken) {
                tokens.push(doc.data().fcmToken);
            }
        });

        if (tokens.length > 0) {
            const payload = {
                notification: {
                    title: 'New Pickup Request',
                    body: `From: ${pickupData.merchantName}`,
                    click_action: 'FLUTTER_NOTIFICATION_CLICK'
                },
                data: {
                    orderId: context.params.orderId,
                    type: 'new_pickup'
                }
            };

            return admin.messaging().sendToDevice(tokens, payload);
        }

        return null;
    });