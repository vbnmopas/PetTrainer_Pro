import paho.mqtt.client as mqtt

# MQTT 브로커 주소 (로컬에서 실행)
BROKER = "Localhost"  # 모든 네트워크 인터페이스에서 수신 가능
PORT = 1883
TOPIC = "esp32/speaker"

# 메시지 수신 콜백 함수
def on_message(client, userdata, msg):
    print(f"📩 Received message on {msg.topic}: {msg.payload.decode()}")

# MQTT 클라이언트 설정
client = mqtt.Client()
client.on_message = on_message

# 브로커 연결 및 구독
client.connect(BROKER, PORT, 60)
client.subscribe(TOPIC)

print(f"✅ MQTT Server listening on {TOPIC}...")
client.loop_forever()  # 계속 실행
