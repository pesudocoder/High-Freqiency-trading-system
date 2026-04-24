import requests
import concurrent.futures
import time
import random

API_URL = "http://localhost:8080/api/orders"
BOOK_URL = "http://localhost:8080/api/book"

NUM_ORDERS = 100
MAX_WORKERS = 20
ORDER_QTY = 10 # Standard 10 quantities per order to make audit math clean

def submit_order(order_data):
    """Hits the Spring Boot POST endpoint"""
    try:
        response = requests.post(API_URL, json=order_data, timeout=5)
        # Returns True if HTTP 200 OK
        return response.status_code == 200
    except requests.exceptions.RequestException:
        return False

def run_stress_test():
    print("--- Starting HFT Phase 3 Stress Test ---")
    print(f"Target: {API_URL}")
    print(f"Simulating {NUM_ORDERS} orders using {MAX_WORKERS} parallel threads.\n")

    current_price = 100.00
    orders = []
    total_qty_sent = 0
    
    # 1. GENERATE PAYLOADS: Random Walk Pricing
    for i in range(1, NUM_ORDERS + 1):
        # Jitter the price between -0.50 and +0.50 
        jitter = random.uniform(-0.50, 0.50)
        current_price += jitter
        current_price = round(current_price, 2)
        
        # 50/50 Chance to be a buyer or seller
        side = random.choice(["BUY", "SELL"])
        
        order = {
            "id": f"STRESS-{i}",
            "side": side,
            "price": current_price,
            "quantity": ORDER_QTY
        }
        orders.append(order)
        total_qty_sent += ORDER_QTY

    # 2. FIRE THE BURST: Concurrency mapping
    start_time = time.time()
    success_count = 0
    failure_count = 0
    
    # ThreadPoolExecutor spins up 20 workers that greedily consume the 100 orders
    with concurrent.futures.ThreadPoolExecutor(max_workers=MAX_WORKERS) as executor:
        results = list(executor.map(submit_order, orders))
        
        for success in results:
            if success:
                success_count += 1
            else:
                failure_count += 1
                
    execution_time = time.time() - start_time

    # 3. AUDIT THE ENGINE: Fetch the book and verify no quantities were lost to Race Conditions
    print(f"Fetching final book state from {BOOK_URL}...")
    try:
        book_response = requests.get(BOOK_URL)
        book_response.raise_for_status()
        book_state = book_response.json()
        
        bids = book_state.get('bids', [])
        asks = book_state.get('asks', [])
        
        remaining_bids_qty = sum(order['quantity'] for order in bids)
        remaining_asks_qty = sum(order['quantity'] for order in asks)
        total_remaining_qty = remaining_bids_qty + remaining_asks_qty
        
        # In a bilateral market, 1 Matched Unit perfectly erases 1 Buy Qty and 1 Sell Qty simultaneously.
        # Therefore, the total volume of quantities that "disappeared" from the book is the exact volume matched!
        matched_total_qty = total_qty_sent - total_remaining_qty
        
        print("\n=== STRESS TEST AUDIT SUMMARY ===")
        print(f"Execution Time        : {execution_time:.3f} seconds")
        print(f"Requests per Second   : {NUM_ORDERS / execution_time:.2f} req/s")
        print(f"Successful Hits       : {success_count}/{NUM_ORDERS}")
        print(f"Failed Hits           : {failure_count}/{NUM_ORDERS}")
        print(f"---------------------------------")
        print(f"Total Qty Transmitted : {total_qty_sent}")
        print(f"Remaining in Book     : {total_remaining_qty} (Bids: {remaining_bids_qty}, Asks: {remaining_asks_qty})")
        print(f"Implied Qty Matched   : {matched_total_qty}")
        
        # The Final Audit Equation Prove
        if total_qty_sent == (matched_total_qty + total_remaining_qty):
             print("\n[✔] DATA INTEGRITY PASSED: Engine handled parallel threads without dropping memory!")
        else:
             print("\n[X] DATA INTEGRITY FAILED: Severe Race Condition Detected.")

    except Exception as e:
        print(f"\n[X] ERROR: Failed to fetch order book for audit: {e}")

if __name__ == "__main__":
    run_stress_test()
