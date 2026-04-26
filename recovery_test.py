import requests
import time
import sys

API_URL = "http://localhost:8080/api/orders"
BOOK_URL = "http://localhost:8080/api/book"

def submit_order(order_id, side, price, quantity):
    order = {
        "id": order_id,
        "side": side,
        "price": price,
        "quantity": quantity
    }
    print(f"   -> Submitting: [ID: {order_id}] {side} {quantity} @ Rs {price}")
    try:
        response = requests.post(API_URL, json=order, timeout=5)
        response.raise_for_status()
    except requests.exceptions.RequestException as e:
        print(f"      [X] ERROR: API is down or rejected order: {e}")
        sys.exit(1)

def fetch_book():
    try:
        response = requests.get(BOOK_URL, timeout=5)
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException:
        print(f"      [X] ERROR: Could not fetch order book.")
        sys.exit(1)

def verify_state(expected_bids, expected_asks):
    book = fetch_book()
    bids = book.get('bids', [])
    asks = book.get('asks', [])

    # We only care about the REC- test orders (in case there is old data)
    test_bids = [b for b in bids if str(b['id']).startswith("REC-")]
    test_asks = [a for a in asks if str(a['id']).startswith("REC-")]

    print("\n--- Verifying Book State ---")
    print(f"Found Bids: {test_bids}")
    print(f"Found Asks: {test_asks}")

    # Check length
    if len(test_bids) != len(expected_bids) or len(test_asks) != len(expected_asks):
        print("\n[X] FAILED: Outstanding orders mismatch!")
        return False
        
    # Check values
    for expected in expected_bids:
        match = next((b for b in test_bids if b['id'] == expected['id']), None)
        if not match or match['quantity'] != expected['quantity']:
             print(f"\n[X] FAILED: Missing or corrupted Bid. Expected {expected}")
             return False

    for expected in expected_asks:
        match = next((a for a in test_asks if a['id'] == expected['id']), None)
        if not match or match['quantity'] != expected['quantity']:
             print(f"\n[X] FAILED: Missing or corrupted Ask. Expected {expected}")
             return False
             
    print("[✔] SUCCESS: Order book state perfectly matches mathematical expectations.")
    return True

def run_test():
    print("=========================================================")
    print(" Phase 4: EVENT SOURCING & WAL CRASH RECOVERY SIMULATION ")
    print("=========================================================\n")
    
    print("Ensure the Spring Boot server is RUNNING before starting.")
    input("Press ENTER to begin Phase 1 (Ingestion)...")

    print("\n--- PHASE 1: INGESTION (Edge Cases & Partial Matches) ---")
    # 1. Base Buy Order
    submit_order("REC-BUY-1", "BUY", 100.0, 10)
    
    # 2. Higher Price Buy Order (Jumps to top of PriorityQueue Max-Heap)
    submit_order("REC-BUY-2", "BUY", 105.0, 10)
    
    # 3. High Price Sell Order (Does not match anything, sits in Ask queue)
    submit_order("REC-SELL-1", "SELL", 110.0, 5)
    
    # 4. Partial Match Edge Case (Sweeps the book!)
    # Meets the 105.0 Bid (matches 10) -> 5 Qty left over
    # Meets the 100.0 Bid (matches 5) -> 0 Qty left over (REC-BUY-1 left with 5)
    submit_order("REC-SELL-2", "SELL", 100.0, 15)

    # Expected State:
    expected_bids = [{"id": "REC-BUY-1", "quantity": 5}]
    expected_asks = [{"id": "REC-SELL-1", "quantity": 5}]

    if not verify_state(expected_bids, expected_asks):
        sys.exit(1)


    print("\n=========================================================")
    print(" 🛑 PHASE 2: DISASTER SIMULATION (SERVER CRASH)")
    print("=========================================================")
    print("1. Go to your Spring Boot terminal in VS Code.")
    print("2. Kill the server by pressing CTRL+C (or stop button).")
    print("-> This will completely wipe the RAM-based PriorityQueues.")
    print("-> Data should seemingly be lost forever.")
    input("Press ENTER here ONLY AFTER you have killed the server...")


    print("\n=========================================================")
    print(" ♻️ PHASE 3: REHYDRATION (SERVER BOOT)")
    print("=========================================================")
    print("1. Go to your Spring Boot terminal in VS Code.")
    print("2. Restart the server (e.g. `mvn spring-boot:run` or hit Play).")
    print("-> Look at the console logs! You should see 'RECOVERING STATE FROM WAL'.")
    print("-> The Projector must automatically replay the JSON events from events.log.")
    input("Press ENTER here ONLY AFTER the server says 'Started...'")
    
    print("\n--- Testing Rehydrated Database ---")
    if verify_state(expected_bids, expected_asks):
         print("\n=========================================================")
         print(" 🚀 PHASE 4 SUCCESS: EVENT SOURCING WORKS PERFECTLY! ")
         print(" Both the state and the edge-cases survived the system crash ")
         print(" without using a relational SQL database. ")
         print("=========================================================")
    else:
         print("\n[X] FAILURE! The WAL failed to reconstruct the PriorityQueues properly.")

if __name__ == "__main__":
    run_test()
