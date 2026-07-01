import websocket
import threading
import time
import json

def on_message(ws, message):
    if message == "\n":
         return

    parts = message.split("\n\n")
    if len(parts) > 1:
        body = parts[1].replace("\x00", "")
        # Parse JSON to pretty print Market Depth
        try:
            depth = json.loads(body)
            print("\n================== MARKET DEPTH L2 ==================")
            asks = depth.get("asks", [])
            # Asks are usually displayed descending
            for ask in reversed(asks):
                print(f"    [ASK] Rs {ask['price']:.2f} | Qty: {ask['quantity']}")
            print("-----------------------------------------------------")
            bids = depth.get("bids", [])
            for bid in bids:
                print(f"    [BID] Rs {bid['price']:.2f} | Qty: {bid['quantity']}")
            print("=====================================================\n")
        except Exception as e:
            print(f">>> Raw Data: {body}")

def on_error(ws, error):
    print(f"Error: {error}")

def on_close(ws, close_status_code, close_msg):
    print("### Connection Closed ###")

def on_open(ws):
    print("Connected! Sending STOMP CONNECT...")
    ws.send("CONNECT\naccept-version:1.1,1.0\nheart-beat:10000,10000\n\n\x00")
    time.sleep(1)
    print("Subscribing to /topic/depth...")
    ws.send("SUBSCRIBE\nid:sub-1\ndestination:/topic/depth\n\n\x00")

if __name__ == "__main__":
    websocket.enableTrace(False)
    ws = websocket.WebSocketApp(
        "ws://localhost:8080/ws",
        on_open=on_open,
        on_message=on_message,
        on_error=on_error,
        on_close=on_close
    )
    ws.run_forever()
