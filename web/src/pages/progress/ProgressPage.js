import React, { Component } from 'react'
import { Timeline, TimelineEvent } from 'react-event-timeline'
import styled from 'styled-components'
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

const Wrapper = styled.div`
  width: 100%;
  justify-content: center;
`

export default class ProgressPage extends Component {
  render() {
    const { events = [] } = this.props;

    return (
      <Wrapper>
        <div>
          {
            events.length > 0 &&
            <Timeline>
              {
                events.map(e => {
                  return (
                    <TimelineEvent title={<span><b>Isuru</b> recieved a badge</span>} 
                      iconColor="#03a9f4"
                      subtitle="2018-07-12 08:56 PM"
                      icon={<FontAwesomeIcon icon="award" />} />
                  )
                })
              }
            </Timeline>
          }
        </div>
      </Wrapper>
    )
  }
}
