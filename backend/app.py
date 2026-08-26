from flask import Flask, request, jsonify
from flask_cors import CORS
import re

app = Flask(__name__)
CORS(app)


# ==========================================
# CUSTOMER SUPPORT INTENTS
# ==========================================

INTENTS = {

    "greeting": {
        "keywords": [
            "hello",
            "hi",
            "hey",
            "good morning",
            "good evening"
        ],
        "response":
            "Hello! 👋 How can I help you today?"
    },

    "order_status": {
        "keywords": [
            "order status",
            "where is my order",
            "track order",
            "order tracking",
            "delivery status",
            "track my package",
            "package"
        ],
        "response":
            "I can help you track your order. "
            "Please provide your order ID, for example ORD12345."
    },

    "refund": {
        "keywords": [
            "refund",
            "money back",
            "return money",
            "refund status",
            "reimbursement"
        ],
        "response":
            "I can help with your refund. "
            "Please provide your order ID so we can check the refund status."
    },

    "payment": {
        "keywords": [
            "payment",
            "payment failed",
            "transaction failed",
            "card declined",
            "upi",
            "payment issue",
            "paid"
        ],
        "response":
            "For a failed payment, please verify your payment method "
            "and try again. If money was deducted, please share the "
            "transaction reference with customer support."
    },

    "delivery": {
        "keywords": [
            "delivery",
            "shipping",
            "ship",
            "courier",
            "delivered",
            "delivery time",
            "when will it arrive"
        ],
        "response":
            "Standard delivery usually takes 3–7 business days. "
            "The exact delivery time depends on your order and location."
    },

    "account": {
        "keywords": [
            "account",
            "login",
            "sign in",
            "password",
            "forgot password",
            "profile",
            "change password"
        ],
        "response":
            "For account problems, use the Forgot Password option "
            "on the login screen. For other account changes, "
            "please contact customer support."
    },

    "contact_support": {
        "keywords": [
            "human",
            "agent",
            "customer care",
            "customer support",
            "contact support",
            "talk to someone",
            "representative"
        ],
        "response":
            "Sure! A customer support representative can help you. "
            "Please provide your name, order ID, and a short description "
            "of your problem."
    },

    "thanks": {
        "keywords": [
            "thanks",
            "thank you",
            "thankyou",
            "thx"
        ],
        "response":
            "You're welcome! 😊 Is there anything else I can help you with?"
    }
}


# ==========================================
# FALLBACK RESPONSE
# ==========================================

FALLBACK_RESPONSE = (
    "I'm not completely sure what you mean. 🤔\n\n"
    "You can ask me about:\n"
    "• Order status\n"
    "• Refunds\n"
    "• Payments\n"
    "• Delivery\n"
    "• Account problems\n"
    "• Customer support"
)


# ==========================================
# TEXT NORMALIZATION
# ==========================================

def normalize(text):

    text = text.lower().strip()

    text = re.sub(
        r"[^a-z0-9\s]",
        " ",
        text
    )

    text = re.sub(
        r"\s+",
        " ",
        text
    )

    return text


# ==========================================
# INTENT DETECTION
# ==========================================

def detect_intent(message):

    text = normalize(message)

    best_intent = "fallback"
    best_score = 0

    for intent, data in INTENTS.items():

        score = 0

        for keyword in data["keywords"]:

            keyword = normalize(keyword)

            if keyword in text:

                # Longer phrases get a higher score
                score += max(
                    1,
                    len(keyword.split())
                )

        if score > best_score:

            best_score = score
            best_intent = intent

    # Calculate confidence
    confidence = min(
        1.0,
        best_score / 3.0
    )

    if best_score == 0:

        return "fallback", 0.0

    return best_intent, confidence


# ==========================================
# HEALTH CHECK
# ==========================================

@app.route("/health", methods=["GET"])
def health():

    return jsonify({

        "status": "ok",

        "service":
            "Customer Support Chatbot"

    })


# ==========================================
# CHAT API
# ==========================================

@app.route("/chat", methods=["POST"])
def chat():

    data = request.get_json(
        silent=True
    ) or {}

    message = str(
        data.get("message", "")
    ).strip()

    # Empty message
    if not message:

        return jsonify({

            "intent": "fallback",

            "confidence": 0.0,

            "response":
                "Please type a message so I can help you."

        }), 400


    # Detect intent
    intent, confidence = detect_intent(
        message
    )


    # Select response
    if intent == "fallback":

        response = FALLBACK_RESPONSE

    else:

        response = INTENTS[
            intent
        ]["response"]


    # Structured response
    return jsonify({

        "intent": intent,

        "confidence":
            round(confidence, 2),

        "response": response

    })


# ==========================================
# START SERVER
# ==========================================

if __name__ == "__main__":

    print(
        "Customer Support Chatbot backend "
        "running on http://0.0.0.0:5000"
    )

    app.run(
        host="0.0.0.0",
        port=5000,
        debug=True
    )