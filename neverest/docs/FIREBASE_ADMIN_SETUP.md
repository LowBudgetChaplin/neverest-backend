# Firebase Admin Setup (Custom Claims)

Pentru endpoint-urile admin din backend (`ROLE_ADMIN`), ai nevoie de custom claims pe userii Firebase.

## 1) Creeaza service account key

In Firebase Console:
- Project Settings -> Service accounts
- Generate new private key
- salveaza fisierul JSON local (nu il urca in git)

## 2) Script rapid pentru setare claim

Exemplu Node.js (ruleaza local, o singura data per user):

```javascript
const admin = require("firebase-admin");

admin.initializeApp({
  credential: admin.credential.cert(require("./service-account.json"))
});

async function setAdmin(uid) {
  await admin.auth().setCustomUserClaims(uid, {
    role: "ADMIN",
    roles: ["ADMIN", "USER"]
  });
  console.log("Admin claims set for:", uid);
}

setAdmin("FIREBASE_UID_HERE")
  .then(() => process.exit(0))
  .catch((err) => {
    console.error(err);
    process.exit(1);
  });
```

## 3) Important pentru client

Dupa ce setezi claims:
- userul trebuie sa faca `sign out`/`sign in` sau token refresh,
- altfel backend-ul nu vede noile roluri imediat.

## 4) Verificare backend

- setezi in backend:
  - `neverest.auth.provider=firebase`
  - `neverest.firebase.project-id=<project-id>`
- apelezi `GET /api/v1/auth/me`
- verifici ca apare `ROLE_ADMIN` la `authorities`.
