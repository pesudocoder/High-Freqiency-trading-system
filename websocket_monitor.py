import websocket
import threading
import time

# To communicate with Spring Boot's WebSocketMessageBroker over raw websockets, 
# we must manually construct and send STOMP protocol text frames.
# A STOMP frame always ends with a NULL byte (\x00).

def on_message(ws, message):
    # Spring Boot sends heartbeats as a single newline character
    if message == "\n":
         return

    print("\n[LIVE TRADE] Raw STOMP Message Received:")
    # Clean the message by splitting at double newline to get just the JSON body payload
    parts = message.split("\n\n")
    if len(parts) > 1:
        body = parts[1].replace("\x00", "") # Remove trailing null byte
        print(f">>> {body}")
    else:
        print(message)

def on_error(ws, error):
    print(f"Error: {error}")

def on_close(ws, close_status_code, close_msg):
    print("### WebSocket connection closed ###")

def on_open(ws):
    print("WebSocket connected. Sending STOMP CONNECT frame...")
    
    # 1. Send CONNECT frame
    connect_frame = "CONNECT\naccept-version:1.1,1.0\nheart-beat:10000,10000\n\n\x00"
    ws.send(connect_frame)

    # Note: A robust system waits for the CONNECTED frame from the server first,
    # but for this script we will simply sleep for 1 second before subscribing.
    time.sleep(1)

    print("Subscribing to /topic/trades...")
    # 2. Send SUBSCRIBE frame
    subscribe_frame = "SUBSCRIBE\nid:sub-0\ndestination:/topic/trades\n\n\x00"
    ws.send(subscribe_frame)
    print("Listening for trades...")

if __name__ == "__main__":
    websocket.enableTrace(False) # Set to True for deep debugging
    ws = websocket.WebSocketApp(
        "ws://localhost:8080/ws",
        on_open=on_open,
        on_message=on_message,
        on_error=on_error,
        on_close=on_close
    )
    
    # Run the connection on a blocking infinite loop
    ws.run_forever()
