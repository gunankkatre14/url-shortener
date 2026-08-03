import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

const withRedis = new Trend('redis_ms', true);

export const options = {
  stages: [
    { duration: '10s', target: 10  }, // warmup
    { duration: '20s', target: 30  }, // load
    { duration: '20s', target: 50  }, // peak
    { duration: '10s', target: 0   }, // cooldown
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed:   ['rate<0.01'],
  },
};

// APNA SHORT CODE DAALO
const SHORT_CODE = 'bfP3Q0';

export default function () {
  const res = http.get(
    `http://localhost:8080/${SHORT_CODE}`,
    { redirects: 0 }
  );

  withRedis.add(res.timings.duration);

  check(res, {
    'status 302':  (r) => r.status === 302,
    'under 100ms': (r) => r.timings.duration < 100,
    'under 20ms':  (r) => r.timings.duration < 20,
  });

  sleep(0.05);
}

export function handleSummary(data) {
  const d = data.metrics;
  console.log('\n========== REDIS PERFORMANCE ==========');
  console.log(`Total requests:  ${d.http_reqs.values.count}`);
  console.log(`Requests/sec:    ${d.http_reqs.values.rate.toFixed(1)}`);
  console.log(`Avg latency:     ${d.http_req_duration.values.avg.toFixed(1)}ms`);
  console.log(`Median:          ${d.http_req_duration.values.med.toFixed(1)}ms`);
  console.log(`p95:             ${d.http_req_duration.values['p(95)'].toFixed(1)}ms`);
  console.log(`p99:             ${d.http_req_duration.values['p(99)'].toFixed(1)}ms`);
  console.log(`Error rate:      ${(d.http_req_failed.values.rate*100).toFixed(2)}%`);
  console.log('========================================\n');
  return {};
}