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
import title from '../assets/title.png';
import icon from '../assets/icon.png';

export default function Header({ showNavLinks = true }) {
  return (
    <header className="bg-white shadow-sm py-4 px-12 flex flex-col sm:flex-row justify-between items-center">
      <Link to="/" className="inline-flex items-center">
        <img
          src={icon}
          alt="InWheel Logo"
          className="h-14 w-14 hover:opacity-90 transition-opacity"
        />
        <img
          src={title}
          alt="InWheel Logo title"
          className="h-14 hover:opacity-90 transition-opacity"
        />
      </Link>

      {showNavLinks && (
        <nav className="space-x-6">
          <a
            href="https://github.com/koinsaari/inwheel"
            target="_blank"
            rel="noopener noreferrer"
            className="text-gray-700 hover:text-[#0e65b3] transition-colors"
          >
            GitHub
          </a>
          <Link
            to="/privacy-policy"
            className="text-gray-700 hover:text-[#0e65b3] transition-colors"
          >
            Privacy Policy
          </Link>
          <Link
            to="/terms-of-service"
            className="text-gray-700 hover:text-[#0e65b3] transition-colors"
          >
            Terms of Service
          </Link>
        </nav>
      )}
    </header>
  );
}
