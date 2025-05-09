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

import { Link } from 'react-router-dom';
import Header from '../components/Header';

export default function PrivacyPolicy() {
  return (
    <div className="min-h-screen flex flex-col bg-gray-50">
      <Header />

      <main className="flex-1 max-w-4xl mx-auto py-12 px-6">
        <h2 className="text-2xl font-bold mb-2 text-gray-800">Privacy Policy</h2>
        <p className="text-gray-600 italic mb-8">Last Modified: 9 May 2025</p>

        {/* Introduction */}
        <div className="space-y-8 text-gray-700 text-justify">
          <section>
            <h3 className="text-xl font-semibold mb-2">Introduction</h3>
            <p>
              This Privacy Policy explains how InWheel handles information when you use the app. For
              other legal terms and disclaimers, please refer to our{' '}
              <Link to="/terms-of-service" className="text-blue-600 hover:underline">
                Terms of Service
              </Link>
              .
            </p>
          </section>

          {/* What data we collect */}
          <section>
            <h3 className="text-xl font-semibold mb-2">What Data We Collect</h3>
            <p>
              InWheel does not collect or store any personal data directly. The app may temporarily
              access your device's location to determine nearby accessibility information, but this
              data is not stored or sent to our servers.
            </p>
            <p>
              Our backend provider (Supabase) may automatically log technical data such as IP
              addresses and request metadata, solely for security and operational diagnostics. This
              data is not linked to your identity.
            </p>
          </section>

          <section>
            <h3 className="text-xl font-semibold mb-2">How We Use Data</h3>
            <p>
              Your device's location is used only to determine which places to display on the map.
              Logged technical data is used to maintain app performance and troubleshoot errors. No
              personal profiles or behavioral tracking is conducted.
            </p>
          </section>

          {/* Third-party services */}
          <section>
            <h3 className="text-xl font-semibold mb-2">Third-Party Services</h3>
            <p>
              We use third-party services that may process limited data under their own privacy
              policies:
            </p>
            <ul className="list-disc pl-5 mt-2 space-y-1">
              <li>
                Google Maps for location services and map rendering.{' '}
                <a
                  href="https://policies.google.com/privacy"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-blue-600 hover:underline"
                >
                  View Google's Privacy Policy
                </a>
                .
              </li>
              <li>
                Firebase for analytics and core functionality.{' '}
                <a
                  href="https://firebase.google.com/support/privacy"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-blue-600 hover:underline"
                >
                  View Firebase's Privacy Policy
                </a>
                .
              </li>
              <li>
                Supabase for backend storage and data access.{' '}
                <a
                  href="https://supabase.com/privacy"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-blue-600 hover:underline"
                >
                  View Supabase's Privacy Policy
                </a>
                .
              </li>
            </ul>
          </section>

          {/* Your rights */}
          <section>
            <h3 className="text-xl font-semibold mb-2">Your Rights</h3>
            <p>
              While we do not store personal data, you may disable location access via your device
              settings at any time. If you believe we have inadvertently received personal data,
              please contact us to request deletion.
            </p>
            <p>
              Contact:{' '}
              <a href="mailto:info@inwheel.ch" className="text-blue-600 hover:underline inline">
                info@inwheel.ch
              </a>
            </p>
          </section>

          <section>
            <h3 className="text-xl font-semibold mb-2">Changes to This Policy</h3>
            <p>
              We may update this policy periodically. Changes will be posted within the app or on
              our website. Continued use after updates implies acceptance.
            </p>
          </section>
        </div>
      </main>

      <footer className="bg-gray-800 text-white py-6 px-6 text-center">
        <p>
          &copy; 2025{' '}
          <a
            href="https://www.linkedin.com/in/aarokoinsaari/"
            target="_blank"
            rel="noopener noreferrer"
            className="!text-white"
          >
            Aaro Koinsaari
          </a>
        </p>
      </footer>
    </div>
  );
}
