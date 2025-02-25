@RestController
@RequestMapping("/api")
public class SmsWebhookController {

    @PostMapping("/sms-webhook")
    public void receiveSms(@RequestParam("From") String from,
                           @RequestParam("Body") String body) {
        // Example: parse "UNLOCK 1234 PIN123"
        // 1. Validate phone number 'from'
        // 2. Parse message => command, lockerId, maybe PIN
        // 3. Check DB if phone number is assigned to that locker
        // 4. If valid, call service to unlock/lock
        // 5. Optionally respond to Twilio with <Response> TwiML
    }
}
