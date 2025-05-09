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

export default function TermsOfService() {
  return (
    <div className="min-h-screen flex flex-col bg-gray-50">
      <Header />

      <main className="flex-1 max-w-4xl mx-auto py-12 px-6">
        <h2 className="text-2xl font-bold mb-2 text-gray-800">Terms of Service</h2>
        <p className="text-gray-600 italic mb-8">Last Modified: 9 May 2025</p>

        <div className="space-y-8 text-gray-700 text-justify">
          {/* Acceptance */}
          <section>
            <h3 className="text-xl font-semibold mb-2">Acceptance of Terms</h3>
            <p>
              By downloading, installing, or using the InWheel application ("App"), you indicate
              that you have read, understood, and agree to be bound by these Terms of Service. If
              you do not agree to these Terms, please do not use the App.
            </p>
            <p>
              For the purposes of these Terms, “we,” “us,” or “InWheel” refers to the App and its
              developer(s), including future contributors.
            </p>
          </section>

          {/* Age requirement & privacy */}
          <section>
            <h3 className="text-xl font-semibold mb-2">Age Requirement</h3>
            <p>
              You must be at least 16 years old—or the minimum age of digital consent in your
              jurisdiction (13 years old in the United States under COPPA)—to use this App. If you
              are younger, you may use the App only with verifiable parental consent.
            </p>
            <p>
              The App does not collect or store any personal data. However, our backend service
              provider (Supabase) may automatically log limited technical data such as IP addresses
              and request metadata, which are used solely for security and operational purposes.
              This data is not used to identify or track users and is handled in accordance with
              Supabase's own privacy policy.
            </p>
            <p>
              Additionally, third-party services used by the App (such as Google Maps and Firebase)
              may process limited data as described in their respective privacy policies. Users
              under 16 should review those policies with a parent or guardian.
            </p>
          </section>

          {/* Acceptable use */}
          <section>
            <h3 className="text-xl font-semibold mb-2">Acceptable Use</h3>
            <p>When using InWheel, you agree to:</p>
            <ul className="list-disc pl-5 mt-2 space-y-1">
              <li>
                Provide accurate and truthful information about accessibility features of places
              </li>
              <li>Respect the privacy, intellectual-property, and other rights of others</li>
              <li>Not impersonate any person or entity</li>
              <li>Not use the App for any illegal or unauthorized purpose</li>
              <li>Not attempt to probe, scan, or test the vulnerability of our systems</li>
              <li>Not upload or transmit viruses, malware, or other malicious code</li>
              <li>Not interfere with or disrupt the App or servers connected to the App</li>
              <li>
                Not post content that is hateful, discriminatory, obscene, defamatory, infringing,
                or otherwise objectionable
              </li>
            </ul>
            <p className="mt-2">
              We reserve the right to terminate or restrict your access to the App if you violate
              these Terms or use the App in a manner that could cause us legal liability.
            </p>
          </section>

          {/* User content */}
          <section>
            <h3 className="text-xl font-semibold mb-2">User Content</h3>
            <p>
              InWheel allows users to submit, post, and share accessibility information ("User
              Content"). By posting User Content, you:
            </p>
            <ul className="list-disc pl-5 mt-2 space-y-1">
              <li>
                Retain ownership of your content but grant InWheel a worldwide, non-exclusive,
                royalty-free license (with the right to sublicense) to use, reproduce, modify,
                adapt, publish, display, and distribute the content in connection with providing and
                improving the App and related services
              </li>
              <li>Represent that you own or have the necessary rights to the content you post</li>
              <li>Understand that all User Content is public and may be viewed by other users</li>
              <li>
                Accept full responsibility for your User Content and any consequences of posting it
              </li>
            </ul>
            <p className="mt-2">
              InWheel does not endorse or verify User Content and disclaims all liability for
              inaccuracies, errors, or objectionable material. We may remove any User Content at our
              sole discretion without notice.
            </p>
            <p className="mt-2">
              User-contributed accessibility data is stored in our database and, where applicable,
              is made available under the{' '}
              <a
                href="https://opendatacommons.org/licenses/odbl/1-0/"
                target="_blank"
                rel="noopener noreferrer"
                className="text-blue-600 hover:underline inline"
              >
                Open Database License (ODbL)
              </a>
              . While we use reasonable efforts to preserve this data, we do not guarantee its
              permanent availability, integrity, or backup.
            </p>
            <p className="mt-2">
              This app includes data from{' '}
              <a
                href="https://www.openstreetmap.org/"
                target="_blank"
                rel="noopener noreferrer"
                className="text-blue-600 hover:underline inline"
              >
                OpenStreetMap
              </a>
              , which is made available under the{' '}
              <a
                href="https://opendatacommons.org/licenses/odbl/1-0/"
                target="_blank"
                rel="noopener noreferrer"
                className="text-blue-600 hover:underline inline"
              >
                Open Database License (ODbL)
              </a>
              .
            </p>
          </section>

          {/* Disclaimer */}
          <section>
            <h3 className="text-xl font-semibold mb-2">Disclaimer</h3>
            <p>
              The App is provided as a community-driven, non-commercial tool for sharing
              accessibility information. It may contain bugs or incomplete information. We do not
              guarantee the accuracy, completeness, or availability of any features or content. Use
              of the App is entirely at your own risk.
            </p>
          </section>

          {/* Limitation of liability */}
          <section>
            <h3 className="text-xl font-semibold mb-2">Limitation of Liability</h3>
            <p>To the maximum extent permitted by law:</p>
            <ul className="list-disc pl-5 mt-2 space-y-1">
              <li>
                The App is provided “as is” and “as available,” without warranties of any kind,
                express or implied.
              </li>
              <li>
                We do not guarantee the accuracy, completeness, or reliability of any content,
                including User Content.
              </li>
              <li>
                InWheel and its developer(s) disclaim all liability for any direct, indirect,
                incidental, special, consequential, or punitive damages arising out of or related to
                your use of the App. You assume all risks associated with using or relying on any
                content within the App.
              </li>
              <li>
                Nothing in these Terms limits liability for gross negligence, wilful misconduct, or
                death or personal injury caused by negligence to the extent such liability cannot be
                excluded under applicable law.
              </li>
              <li>
                Where liability cannot be fully excluded, our total liability shall not exceed CHF
                50 or the minimum amount required by applicable law.
              </li>
            </ul>
            <p className="mt-2">
              Accessibility information may not always be current or accurate; you accept all risks
              of relying on it.
            </p>
          </section>

          {/* Force Majeure */}
          <section>
            <h3 className="text-xl font-semibold mb-2">Force Majeure</h3>
            <p>
              We are not liable for any failure or delay in performance caused by events beyond our
              reasonable control, including natural disasters, outages, or third-party technical
              failures.
            </p>
          </section>

          {/* Indemnity */}
          <section>
            <h3 className="text-xl font-semibold mb-2">Indemnity</h3>
            <p>
              You agree to indemnify, defend, and hold harmless InWheel, its developers,
              contributors, and affiliates from and against any claims, damages, liabilities,
              losses, costs, or expenses (including <strong>reasonable</strong> legal fees) arising
              from:
            </p>
            <ul className="list-disc pl-5 mt-2 space-y-1">
              <li>Your use or misuse of the App</li>
              <li>Your violation of these Terms</li>
              <li>Your submission of any User Content</li>
              <li>Your violation of any rights of a third party</li>
            </ul>
          </section>

          {/* Termination */}
          <section>
            <h3 className="text-xl font-semibold mb-2">Termination</h3>
            <p>
              We may suspend or terminate your access to the App at any time, without notice, if you
              breach these Terms or use the App in a manner that could cause us legal liability.
            </p>
          </section>

          {/* Governing law & dispute resolution */}
          <section>
            <h3 className="text-xl font-semibold mb-2">Governing Law and Dispute Resolution</h3>
            <p>
              These Terms are governed by and construed in accordance with the laws of Switzerland,
              without regard to its conflict-of-law principles.
            </p>
            <p className="mt-2">
              Any dispute, controversy, or claim arising out of or in relation to these
              Terms—including their validity, breach, or termination—shall be resolved by
              arbitration in accordance with the{' '}
              <a
                href="https://www.swissarbitration.org/wp-content/uploads/2023/08/Swiss-Rules-2021-EN.pdf"
                target="_blank"
                rel="noopener noreferrer"
                className="text-blue-600 underline"
              >
                Swiss Rules of International Arbitration (2021)
              </a>{' '}
              of the Swiss Arbitration Centre in force on the date the Notice of Arbitration is
              submitted.
            </p>
            <p className="mt-2">
              The number of arbitrators shall be one.
              <br />
              The seat of the arbitration shall be Geneva, Switzerland.
              <br />
              The arbitration shall be conducted in English.
              <br />
              Unless the arbitrator decides otherwise, the parties shall share arbitration costs
              equally.
            </p>
            <p className="mt-2">
              You agree any claim must be brought in your individual capacity and not as part of any
              class or representative action.
            </p>
          </section>

          {/* Availability & updates */}
          <section>
            <h3 className="text-xl font-semibold mb-2">Availability and Updates</h3>
            <p>
              We may change, suspend, or discontinue the App—or any part of it—at any time without
              notice. We may also update the App automatically when a new version is available.
            </p>
          </section>

          {/* Changes to terms */}
          <section>
            <h3 className="text-xl font-semibold mb-2">Changes to Terms</h3>
            <p>
              We may modify these Terms at any time by posting the updated Terms on our website or
              within the App. For material changes, we will provide at least 14 days' notice unless
              a shorter period is required by law. Your continued use of the App after changes are
              posted constitutes your acceptance of the updated Terms.
            </p>
          </section>

          {/* Third-party links */}
          <section>
            <h3 className="text-xl font-semibold mb-2">Third-Party Links and Services</h3>
            <p>
              The App may contain links to third-party websites or services that we do not control.
              We are not responsible for the content or practices of those third parties.
            </p>
          </section>

          {/* Severability */}
          <section>
            <h3 className="text-xl font-semibold mb-2">Severability</h3>
            <p>
              If any part of these Terms is held to be invalid or unenforceable, the remaining
              provisions will continue in full force.
            </p>
          </section>

          {/* Entire agreement */}
          <section>
            <h3 className="text-xl font-semibold mb-2">Entire Agreement</h3>
            <p>
              These Terms constitute the entire agreement between you and the developer regarding
              use of the App.
            </p>
          </section>

          {/* No waiver */}
          <section>
            <h3 className="text-xl font-semibold mb-2">No Waiver</h3>
            <p>
              Failure to enforce any right or provision in these Terms does not waive that right.
            </p>
          </section>

          {/* Contact */}
          <section>
            <h3 className="text-xl font-semibold mb-2">Contact Us</h3>
            <p>
              If you have any questions about these Terms, please contact us at{' '}
              <a href="mailto:info@inwheel.ch" className="text-blue-600 hover:underline inline">
                info@inwheel.ch
              </a>
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
