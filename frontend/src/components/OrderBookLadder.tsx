import React from 'react';

type DepthLevel = { price: number; quantity: number };

interface Props {
  asks: DepthLevel[];
  bids: DepthLevel[];
}

export const OrderBookLadder: React.FC<Props> = ({ asks = [], bids = [] }) => {
  // Asks are typically rendered descending, highest price at top, lowest out closest to spread
  const sortedAsks = [...asks].sort((a, b) => b.price - a.price);
  
  return (
    <div className="bg-slate-800 rounded-xl shadow-lg border border-slate-700 p-4 h-full flex flex-col">
      <h2 className="text-xl font-bold text-slate-200 mb-4 tracking-wide border-b border-slate-700 pb-2">Order Book (L2)</h2>
      
      <div className="flex text-xs text-slate-400 font-semibold mb-2 px-2 uppercase tracking-wider">
        <div className="flex-1 text-left">Price (Rs)</div>
        <div className="flex-1 text-right">Quantity</div>
      </div>

      <div className="flex-1 overflow-auto space-y-px hide-scrollbar font-mono text-sm">
        {/* Asks (Sells) */}
        <div className="flex flex-col justify-end min-h-[50%] pb-2">
          {sortedAsks.length === 0 && <div className="text-center text-slate-500 italic py-4">No Asks</div>}
          {sortedAsks.map((ask, idx) => (
            <div key={`ask-${idx}`} className="flex px-2 py-1 hover:bg-slate-700/50 transition-colors group cursor-default">
              <div className="flex-1 text-red-500 font-semibold">{ask.price.toFixed(2)}</div>
              <div className="flex-1 text-right text-slate-300">{ask.quantity}</div>
              {/* Fake graphical depth bar */}
              <div className="absolute right-4 h-5 bg-red-900/20 z-0 pointer-events-none group-hover:bg-red-900/30 transition-all rounded-sm" style={{ width: `${Math.min(ask.quantity * 2, 80)}%` }} />
            </div>
          ))}
        </div>

        {/* The Spread (Center Line) */}
        <div className="flex items-center justify-center h-8 bg-slate-900/50 rounded text-slate-400 border-y border-slate-700 text-xs font-bold shadow-inner">
          --- MAXIMUM SPREAD BOUNDARY ---
        </div>

        {/* Bids (Buys) */}
        <div className="flex flex-col justify-start pt-2">
          {bids.length === 0 && <div className="text-center text-slate-500 italic py-4">No Bids</div>}
          {bids.map((bid, idx) => (
            <div key={`bid-${idx}`} className="flex px-2 py-1 hover:bg-slate-700/50 transition-colors group cursor-default relative">
              <div className="flex-1 text-emerald-500 font-semibold">{bid.price.toFixed(2)}</div>
              <div className="flex-1 text-right text-slate-300 z-10">{bid.quantity}</div>
              <div className="absolute right-4 h-5 bg-emerald-900/20 z-0 pointer-events-none group-hover:bg-emerald-900/30 transition-all rounded-sm" style={{ width: `${Math.min(bid.quantity * 2, 80)}%` }} />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
