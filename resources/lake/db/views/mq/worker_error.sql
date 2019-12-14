select
    *
from
    mq.message
where
    channel = 'worker-errors';