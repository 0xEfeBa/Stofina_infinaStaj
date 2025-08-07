'use client';

import React from 'react'
import { MOCK_MUSTERILER } from '@/constants/mockCustomer';
import { useState, useMemo } from 'react';

export default function CustomersPage() {
    const [search, setSearch] = useState('');
    const [filter, setFilter] = useState('');
    const [sortBy, setSortBy] = useState('');
    const [limit, setLimit] = useState(5);

    const filtered = useMemo(() => {
        let list = [...MOCK_MUSTERILER];

        if (search) {
            list = list.filter((c) =>
                c.adSoyad.toLowerCase().includes(search.toLowerCase())
            );
        }

        if (filter) {
            list = list.filter((c) => c.musteriTipi === filter);
        }

        if (sortBy === 'portfoy') {
            list.sort((a, b) => b.portfoyBuyuklugu - a.portfoyBuyuklugu);
        } else if (sortBy === 'kayit') {
            list.sort((a, b) =>
                new Date(b.kayitTarihi).getTime() - new Date(a.kayitTarihi).getTime()
            );
        }

        return list.slice(0, limit);
    }, [search, filter, sortBy, limit]);

    return (
        <div className="p-6 bg-gray-50 min-h-screen">
            <div className="flex items-center justify-between mb-6">
                <h1 className="text-2xl font-semibold text-gray-800">Müşteri Listesi</h1>
            </div>

            {/* Arama ve Filtreler */}
            <div className="flex flex-wrap gap-4 mb-4">
                <input
                    type="text"
                    placeholder="Müşteri Ara"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    className="border px-4 py-2 rounded-md text-sm w-full md:w-1/3"
                />

                <select
                    value={filter}
                    onChange={(e) => setFilter(e.target.value)}
                    className="border px-4 py-2 rounded-md text-sm"
                >
                    <option value="">Tüm Tipler</option>
                    <option value="Bireysel">Bireysel</option>
                    <option value="Kurumsal">Kurumsal</option>
                </select>

                <select
                    value={sortBy}
                    onChange={(e) => setSortBy(e.target.value)}
                    className="border px-4 py-2 rounded-md text-sm"
                >
                    <option value="">Sıralama</option>
                    <option value="portfoy">Portföy Büyüklüğü</option>
                    <option value="kayit">Kayıt Tarihi</option>
                </select>
            </div>

            {/* Tablo */}
            <div className="overflow-x-auto bg-white shadow rounded-md">
                <table className="min-w-full divide-y divide-gray-200 text-sm">
                    <thead className="bg-gray-100 text-gray-700">
                        <tr>
                            <th className="px-4 py-3 text-left">Ad Soyad</th>
                            <th className="px-4 py-3 text-left">Tip</th>
                            <th className="px-4 py-3 text-left">Şehir</th>
                            <th className="px-4 py-3 text-left">Portföy</th>
                            <th className="px-4 py-3 text-left">Durum</th>
                            <th className="px-4 py-3 text-left">Kayıt Tarihi</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100">
                        {filtered.map((m) => (
                            <tr key={m.id} className="hover:bg-gray-50 transition">
                                <td className="px-4 py-2 font-medium">{m.adSoyad}</td>
                                <td className="px-4 py-2">{m.musteriTipi}</td>
                                <td className="px-4 py-2">{m.sehir}</td>
                                <td className="px-4 py-2">₺{m.portfoyBuyuklugu.toLocaleString()}</td>
                                <td
                                    className={`px-4 py-2 ${m.durum === 'Aktif' ? 'text-green-600' : 'text-red-500'
                                        }`}
                                >
                                    {m.durum}
                                </td>
                                <td className="px-4 py-2">{m.kayitTarihi}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {/* Daha Fazla Butonu */}
            {filtered.length < MOCK_MUSTERILER.length && (
                <div className="mt-6 text-center">
                    <button
                        onClick={() => setLimit((prev) => prev + 5)}
                        className="px-6 py-2 bg-gray-700 text-white text-sm rounded-md hover:bg-gray-800"
                    >
                        Daha Fazla
                    </button>
                </div>
            )}
        </div>
    );
}