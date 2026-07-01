import React from 'react';

type Trade = {
  initiatorId: string;
  targetId: string;
  side: string;
  price: number;
  quantity: number;
  timestamp: string; // ISO String
};

interface Props {
  trades: Trade[];
}

export const LiveTradesTape: React.FC<Props> = ({ trades }) => {
  return (
    <div className="bg-slate-800 rounded-xl shadow-lg border border-slate-700 p-4 h-full flex flex-col">
      <h2 className="text-xl font-bold text-slate-200 mb-4 tracking-wide border-b border-slate-700 pb-2">Live Tape</h2>
      
      <div className="flex text-xs text-slate-400 font-semibold mb-2 px-1 uppercase tracking-wider">
        <div className="flex-1 text-left">Price</div>
        <div className="w-16 text-right">Qty</div>
        <div className="w-20 text-right">Time</div>
      </div>

      <div className="flex-1 overflow-auto space-y-1 hide-scrollbar font-mono text-sm">
        {trades.length === 0 && <div className="text-slate-500 italic text-center mt-4 text-xs font-sans">No trades recorded...</div>}
        {trades.map((trade, idx) => {
           const time = new Date(trade.timestamp).toLocaleTimeString([], { hour12: false, hour: '2-digit', minute:'2-digit', second:'2-digit', fractionalSecondDigits: 2 });
           const isBuyerInitiated = trade.side === "BUY";
           
           return (
             <div 
               key={idx} 
               className={`flex px-2 py-1.5 rounded-md animate-fade-in transition-colors ${isBuyerInitiated ? 'bg-emerald-900/10 text-emerald-400' : 'bg-red-900/10 text-red-400'}`}
             >
               <div className="flex-1 font-bold">{trade.price.toFixed(2)}</div>
               <div className="w-16 text-right font-medium">{trade.quantity}</div>
               <div className="w-20 text-right text-xs opacity-75">{time.split('.')[0]}<span className="text-[10px] opacity-50">.{time.split('.')[1]}</span></div>
             </div>
           );
        })}
      </div>
    </div>
  );
};
