/*
 * Copyright © 2025 Aaro Koinsaari
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import Header from '../components/Header';

export default function HomePage() {
  return (
    <div className="min-h-screen flex flex-col bg-gray-50">
      <Header />

      <main className="flex-1">
        {/* Hero */}
        <section className="py-16 px-6">
          <div className="max-w-3xl mx-auto text-center">
            <h2 className="text-3xl sm:text-4xl font-bold text-gray-800 mb-6">
              Help build the world's most detailed accessibility map application
            </h2>
            <p className="text-lg text-gray-600 mb-8">
              InWheel aims to become the best way to find real, practical accessibility information
              about each place you go. It starts with Switzerland and Finland, and with people like
              you 🫵
            </p>

            <a
              href="#download"
              className="!text-white inline-block bg-[#0e65b3] hover:bg-[#0b5596] font-medium px-6 py-3 rounded-lg transition-colors"
            >
              Download Coming Soon
            </a>
          </div>
        </section>

        {/* Features */}
        <section className="py-12 px-6 bg-white">
          <h2 className="text-2xl font-bold text-center mb-10">Features</h2>
          <div className="max-w-5xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="bg-gray-50 p-6 rounded-lg shadow-sm">
              <h3 className="text-xl font-semibold text-[#009b4a] mb-3">
                Detailed Accessibility Data
              </h3>
              <p className="text-gray-600">
                Find real details that matter — entrances, steps, ramps, toilets, and more.
              </p>
            </div>
            <div className="bg-gray-50 p-6 rounded-lg shadow-sm">
              <h3 className="text-xl font-semibold text-[#0e65b3] mb-3">Starting in Europe</h3>
              <p className="text-gray-600">
                Currently covering Finland and Switzerland — with plans to expand across Europe.
              </p>
            </div>
            <div className="bg-gray-50 p-6 rounded-lg shadow-sm">
              <h3 className="text-xl font-semibold text-[#009b4a] mb-3">
                Powered by the Community
              </h3>
              <p className="text-gray-600">
                Anyone can add, update, and verify accessibility data — together we build the map.
              </p>
            </div>
          </div>
        </section>

        {/* Development notice */}
        <section className="bg-amber-50 py-8 px-6">
          <p className="max-w-3xl mx-auto text-center text-amber-800">
            <strong>Note:</strong> This website is still under development. For more information
            about the InWheel app, please visit our GitHub repository.
          </p>
        </section>
      </main>

      {/* Footer */}
      <footer className="bg-gray-800 text-white py-8 px-6">
        <div className="max-w-5xl mx-auto flex justify-center space-x-8">
          <a
            href="https://github.com/koinsaari/inwheel"
            target="_blank"
            rel="noopener noreferrer"
            className="text-gray-300 hover:text-white transition-colors"
          >
            GitHub
          </a>
          <a
            href="mailto:info@inwheel.ch"
            className="text-gray-300 hover:text-white transition-colors"
          >
            Contact
          </a>
        </div>
        <div className="mt-4 text-center text-gray-400">
          <p>
            &copy; 2025{' '}
            <a
              href="https://www.linkedin.com/in/aarokoinsaari/"
              target="_blank"
              rel="noopener noreferrer"
              className="text-gray-300 hover:text-white transition-colors"
            >
              Aaro Koinsaari
            </a>
          </p>
        </div>
      </footer>
    </div>
  );
}
