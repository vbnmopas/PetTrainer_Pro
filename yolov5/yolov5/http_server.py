from flask import Flask, request, jsonify

app = Flask(__name__)

@app.route('/send', methods=['POST'])
def receive_message():
    data = request.get_json()
    message = data.get("message", "")
    print(f"📩 받은 메시지: {message}")
    
    # 여기서 메시지 처리 후 응답
    return jsonify({"status": "success", "received": message}), 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
