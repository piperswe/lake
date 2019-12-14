select
    message.*,
    part.body as body
from
    email.message
inner join email.part
    on part.contenttype ilike '%text/plain%' and part.message = message.id;