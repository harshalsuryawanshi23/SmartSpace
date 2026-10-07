const fs = require('fs'); 
const files = [
  'src/components/common/Navbar.tsx', 
  'src/components/discovery/DecoratorMatches.tsx', 
  'src/components/discovery/NLSearchBar.tsx', 
  'src/components/owner/PricingAdvisor.tsx', 
  'src/components/trust/PrivacyDashboard.tsx', 
  'src/pages/admin/AdminConsole.tsx', 
  'src/pages/decorator/DecoratorDashboard.tsx', 
  'src/pages/owner/OwnerDashboard.tsx'
]; 
for (const f of files) { 
  try { 
    let c = fs.readFileSync(f, 'utf8'); 
    c = 'import { useState, useEffect } from "react";\n' + c; 
    fs.writeFileSync(f, c); 
  } catch(e) {
    console.error(e);
  } 
}
