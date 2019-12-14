select *
from mq.queue
where channel = ?
order by timestamp
limit 1