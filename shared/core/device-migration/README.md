# Easy Device Migration - QR login

- `origin` - the device where the user is logged in, which will verify the user via biometrics,
  scan a QR code and share the session with the target device.
- `target` - the device the user wants to log in, which will present a QR code
  and wait until the session is shared on the origin device.
