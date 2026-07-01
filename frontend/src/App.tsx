import React, { useEffect, useState } from 'react';
import { WebSocketService } from './services/WebSocketService';
import { OrderEntryForm } from './components/OrderEntryForm';
import { OrderBookLadder } from './components/OrderBookLadder';
import { LiveTradesTape } from './components/LiveTradesTape';

export default function App() {
  const [bids, setBids] = useState<any[]>([]);
  const [asks, setAsks] = useState<any[]>([]);
  const [trades, setTrades] = useState<any[]>([]);
  const [isConnected, setIsConnected] = useState(false);

  useEffect(() => {
    const ws = new WebSocketService(
      () => setIsConnected(true),
      () => setIsConnected(false),
      (trade) => {
        // Prepend new trade, keep rolling buffer of 50
        setTrades(prev => [trade, ...prev].slice(0, 50));
      },
      (depth) => {
        setBids(depth.bids || []);
        setAsks(depth.asks || []);
      }
    );

    ws.connect();

    return () => {
      ws.disconnect();
    };
  }, []);

  return (
    <div className="min-h-screen bg-slate-900 text-slate-200 font-sans relative">
      
      {/* Top Header */}
      <header className="bg-slate-800 border-b border-slate-700 p-4 shadow-md flex justify-between items-center z-10 relative">
        <h1 className="text-2xl font-bold tracking-widest text-white shadow-sm flex items-center gap-3">
          <div className="w-6 h-6 rounded-sm bg-gradient-to-tr from-indigo-600 to-purple-600 shadow-lg shadow-indigo-500/30" />
          NEXUS EXCHANGE <span className="text-slate-400 font-light text-sm tracking-normal">| HFT Core</span>
        </h1>
        
        <div className={`flex items-center gap-2 px-3 py-1 rounded-full border text-xs font-bold ${isConnected ? 'bg-emerald-900/30 border-emerald-500/50 text-emerald-400' : 'bg-red-900/30 border-red-500/50 text-red-500 animate-pulse'}`}>
          <div className={`w-2 h-2 rounded-full ${isConnected ? 'bg-emerald-500' : 'bg-red-500'}`} />
          {isConnected ? 'SYSTEM ONLINE' : 'CONNECTION LOST'}
        </div>
      </header>

      {/* Reconnect Overlay Hybrid Behavior */}
      {!isConnected && (
        <div className="absolute inset-x-0 top-[73px] bottom-0 z-50 bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4">
           <div className="bg-slate-800 border border-red-500/50 rounded-xl p-8 shadow-2xl shadow-red-900/20 max-w-md w-full text-center">
              <div className="w-16 h-16 rounded-full bg-red-900/30 flex items-center justify-center mx-auto mb-4 animate-bounce">
                 <div className="w-6 h-6 rounded-full bg-red-500" />
              </div>
              <h2 className="text-2xl font-bold text-white mb-2">Connection Lost</h2>
              <p className="text-slate-400 mb-6">Backend server is offline. Waiting to rehydrate state...</p>
              <div className="h-1.5 w-full bg-slate-700 rounded-full overflow-hidden">
                 <div className="h-full bg-red-500 w-1/3 animate-ping rounded-full" />
              </div>
           </div>
        </div>
      )}

      {/* Main Grid Layout */}
      <main className="max-w-7xl relative mx-auto p-4 md:p-6 h-[calc(100vh-73px)]">
        <div className="grid grid-cols-1 md:grid-cols-3 xl:grid-cols-12 gap-6 h-full">
          
          <div className="xl:col-span-4 h-full">
            <OrderEntryForm />
          </div>

          <div className="xl:col-span-5 h-full">
            <OrderBookLadder bids={bids} asks={asks} />
          </div>

          <div className="xl:col-span-3 h-full">
            <LiveTradesTape trades={trades} />
          </div>

        </div>
      </main>

    </div>
  );
}
