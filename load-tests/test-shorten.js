import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const errorRate = new Rate('error_rate');

// APNA TOKEN YAHAN DAALO
const TOKEN = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTc4MjM5NzU1MywiZXhwIjoxNzgyNDgzOTUzfQ.suiEqy10swHc6kMql83gO8bjYcJpcmR9H4QS0S-RoPg';

export const options = {
  stages: [
    { duration: '10s', target: 5  },
    { duration: '20s', target: 20 },
    { duration: '20s', target: 30 },
    { duration: '10s', target: 0  },
  ],
  thresholds: {
    http_req_duration: ['p(95)<1000'],
    http_req_failed:   ['rate<0.05'],
  },
};

export default function () {
  const res = http.post(
    'http://localhost:8080/api/shorten',
    JSON.stringify({
      originalUrl: `https://www.example.com/page-${__VU}-${__ITER}`,
    }),
    {
      headers: {
        'Content-Type':  'application/json',
        'Authorization': `Bearer ${TOKEN}`,
      },
    }
  );

  const passed = check(res, {
    'status 200':  (r) => r.status === 200,
    'has shortUrl':(r) => {
      try { return !!JSON.parse(r.body).shortUrl; }
      catch(e) { return false; }
    },
    'under 500ms': (r) => r.timings.duration < 500,
  });

  errorRate.add(!passed);
  sleep(0.3);
}

export function handleSummary(data) {
  const d = data.metrics;
  console.log('\n========== SHORTEN TEST RESULTS ==========');
  console.log(`Total requests:   ${d.http_reqs.values.count}`);
  console.log(`Requests/sec:     ${d.http_reqs.values.rate.toFixed(1)}`);
  console.log(`Avg latency:      ${d.http_req_duration.values.avg.toFixed(1)}ms`);
  console.log(`p95 latency:      ${d.http_req_duration.values['p(95)'].toFixed(1)}ms`);
  console.log(`Failed:           ${(d.http_req_failed.values.rate*100).toFixed(2)}%`);
  console.log('==========================================\n');
  return {};
}