import React, { useState } from 'react';
import axios from 'axios';

export const OrderEntryForm: React.FC = () => {
  const [side, setSide] = useState<'BUY' | 'SELL'>('BUY');
  const [price, setPrice] = useState<string>('100.5');
  const [quantity, setQuantity] = useState<string>('10');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    
    // Generate UUID locally for demo
    const id = "ord-" + Math.random().toString(36).substr(2, 6);

    try {
      await axios.post('http://localhost:8080/api/orders', {
        id,
        side,
        price: parseFloat(price),
        quantity: parseInt(quantity)
      });
      // Optionally Clear form or show success toast
    } catch (err) {
      console.error("Order rejected:", err);
      alert("Failed to submit order.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-slate-800 rounded-xl shadow-lg border border-slate-700 p-6 h-full">
      <h2 className="text-xl font-bold text-slate-200 mb-6 tracking-wide border-b border-slate-700 pb-2">Order Entry</h2>
      
      <form onSubmit={handleSubmit} className="space-y-6">
        
        {/* Buy / Sell Toggle */}
        <div className="flex items-center bg-slate-900 rounded-lg p-1.5 shadow-inner">
          <button 
            type="button" 
            onClick={() => setSide('BUY')}
            className={`flex-1 py-2 text-sm font-bold rounded-md transition-all ${side === 'BUY' ? 'bg-emerald-600 text-white shadow-md' : 'text-slate-400 hover:text-slate-200'}`}
          >
            BUY
          </button>
          <button 
            type="button" 
            onClick={() => setSide('SELL')}
            className={`flex-1 py-2 text-sm font-bold rounded-md transition-all ${side === 'SELL' ? 'bg-red-600 text-white shadow-md' : 'text-slate-400 hover:text-slate-200'}`}
          >
            SELL
          </button>
        </div>

        {/* Inputs */}
        <div className="space-y-4 font-mono">
          <div>
            <label className="block text-xs font-semibold text-slate-400 mb-1 uppercase tracking-wider">Price Details (Rs)</label>
            <div className="relative">
              <span className="absolute left-3 top-3 text-slate-500">₹</span>
              <input 
                type="number" 
                step="0.01" 
                required 
                value={price}
                onChange={e => setPrice(e.target.value)}
                className="w-full bg-slate-900 border border-slate-700 rounded-lg py-3 pl-8 pr-4 text-slate-200 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition-colors"
              />
            </div>
          </div>
          
          <div>
            <label className="block text-xs font-semibold text-slate-400 mb-1 uppercase tracking-wider">Quantity Size</label>
            <input 
              type="number" 
              required 
              min="1"
              value={quantity}
              onChange={e => setQuantity(e.target.value)}
              className="w-full bg-slate-900 border border-slate-700 rounded-lg py-3 px-4 text-slate-200 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition-colors"
            />
          </div>
        </div>

        {/* Action Button */}
        <button 
          type="submit" 
          disabled={loading}
          className={`w-full py-4 text-lg font-bold rounded-lg shadow-lg active:scale-95 transition-all text-white ${side === 'BUY' ? 'bg-emerald-600 hover:bg-emerald-500 border border-emerald-500 dark' : 'bg-red-600 hover:bg-red-500 border border-red-500'}`}
        >
          {loading ? 'EXECUTING...' : `SUBMIT ${side} ORDER`}
        </button>

      </form>
    </div>
  );
};
